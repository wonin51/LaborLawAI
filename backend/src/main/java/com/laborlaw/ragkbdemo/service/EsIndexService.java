package com.laborlaw.ragkbdemo.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.laborlaw.ragkbdemo.config.RagAiProperties;
import com.laborlaw.ragkbdemo.exception.EsOperationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class EsIndexService {

    public static final String DEFAULT_INDEX_NAME = "legal_chunk_index_g5";

    private static final int DENSE_VECTOR_MAX_DIMENSION = 2048;
    private static final int CHUNK_MIN_LENGTH = 800;
    private static final int CHUNK_MAX_LENGTH = 1200;
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(15);
    private static final String EMBEDDING_PATH = "/embeddings";
    private static final String INDEX_DOC_PATH = "/_doc/";
    private static final String INDEX_EXISTS_MESSAGE = "\u7d22\u5f15\u5df2\u5b58\u5728";
    private static final String INDEX_CREATED_MESSAGE = "\u7d22\u5f15\u521b\u5efa\u6210\u529f";
    private static final String ES_AUTH_FAILED_MESSAGE =
            "Elasticsearch\u8ba4\u8bc1\u5931\u8d25\uff0c\u8bf7\u68c0\u67e5RAG_AI_ELASTICSEARCH_USERNAME/RAG_AI_ELASTICSEARCH_PASSWORD";
    private static final Pattern ARTICLE_HEADING_PATTERN =
            Pattern.compile("^(\u7b2c[\u4e00-\u9fa5\u3007\u96f6\\d]+\u6761)(.*)$");
    private static final Pattern SECTION_HEADING_PATTERN =
            Pattern.compile("^\u7b2c[\u4e00-\u9fa5\u3007\u96f6\\d]+[\u7f16\u7ae0\u8282\u90e8].*$");

    private final RagAiProperties properties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    @Autowired
    public EsIndexService(RagAiProperties properties, ObjectMapper objectMapper) {
        this(properties, objectMapper, HttpClient.newBuilder().connectTimeout(REQUEST_TIMEOUT).build());
    }

    EsIndexService(RagAiProperties properties, ObjectMapper objectMapper, HttpClient httpClient) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.httpClient = httpClient;
    }

    public String initIndex(String indexName) {
        String normalizedIndexName = normalizeIndexName(indexName);
        if (indexExists(normalizedIndexName)) {
            return INDEX_EXISTS_MESSAGE;
        }
        createIndex(normalizedIndexName);
        return INDEX_CREATED_MESSAGE;
    }

    public int contentVectorDimension() {
        RagAiProperties.ModelEndpoint embedding = properties.getEmbedding();
        Integer configuredDimension = embedding == null ? null : embedding.getDimension();
        if (configuredDimension == null || configuredDimension <= 0) {
            return DENSE_VECTOR_MAX_DIMENSION;
        }
        return Math.min(configuredDimension, DENSE_VECTOR_MAX_DIMENSION);
    }

    public List<LegalKnowledgeChunk> splitLegalDocument(String rawText) {
        if (!StringUtils.hasText(rawText)) {
            return List.of();
        }

        List<ChunkDraft> drafts = new ArrayList<>();
        String normalizedText = rawText.replace("\r\n", "\n").replace('\r', '\n');
        String[] paragraphs = normalizedText.split("\\n+");
        String currentSectionTitle = null;
        String currentArticleNo = null;
        StringBuilder currentContent = new StringBuilder();

        for (String paragraph : paragraphs) {
            String text = paragraph.trim();
            if (!StringUtils.hasText(text)) {
                continue;
            }

            if (isSectionHeading(text)) {
                addDraft(drafts, currentArticleNo, currentSectionTitle, currentContent.toString());
                currentContent.setLength(0);
                currentArticleNo = null;
                currentSectionTitle = text;
                continue;
            }

            Matcher articleMatcher = ARTICLE_HEADING_PATTERN.matcher(text);
            if (articleMatcher.matches()) {
                addDraft(drafts, currentArticleNo, currentSectionTitle, currentContent.toString());
                currentContent.setLength(0);
                currentArticleNo = articleMatcher.group(1);
                currentContent.append(text);
                continue;
            }

            if (currentArticleNo != null) {
                appendParagraph(currentContent, text);
            } else {
                addDraft(drafts, null, currentSectionTitle, text);
            }
        }

        addDraft(drafts, currentArticleNo, currentSectionTitle, currentContent.toString());
        return toChunks(drafts);
    }

    public List<Double> embedText(String content) {
        RagAiProperties.ModelEndpoint embedding = properties.getEmbedding();
        if (embedding == null || !StringUtils.hasText(embedding.getBaseUrl())) {
            throw new EsOperationException(503,
                    "Embedding URL\u672a\u914d\u7f6e\uff0c\u8bf7\u68c0\u67e5RAG_AI_EMBEDDING_BASE_URL");
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", defaultText(embedding.getModel(), "qwen3-embedding:4b"));
        body.put("input", content);

        HttpRequest.Builder builder = HttpRequest.newBuilder(embeddingUri())
                .timeout(REQUEST_TIMEOUT)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(writeJson(body), StandardCharsets.UTF_8));
        addBearerAuth(builder, embedding);

        HttpResponse<String> response = send(builder.build());
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return readEmbedding(response.body());
        }
        if (response.statusCode() == 401 || response.statusCode() == 403) {
            throw new EsOperationException(503, "Embedding\u8ba4\u8bc1\u5931\u8d25\uff0c\u8bf7\u68c0\u67e5RAG_AI_EMBEDDING_API_KEY");
        }
        throw new EsOperationException(503, "Embedding\u751f\u6210\u5931\u8d25\uff1a" + brief(response.body()));
    }

    public String indexDocument(String indexName, String documentId, Map<String, Object> document) {
        HttpRequest.Builder builder = HttpRequest.newBuilder(indexDocumentUri(indexName, documentId))
                .timeout(REQUEST_TIMEOUT)
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(writeJson(document), StandardCharsets.UTF_8));
        addBasicAuth(builder);

        HttpResponse<String> response = send(builder.build());
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return documentId;
        }
        if (response.statusCode() == 401 || response.statusCode() == 403) {
            throw new EsOperationException(503, ES_AUTH_FAILED_MESSAGE);
        }
        throw new IllegalStateException("Elasticsearch document index failed (HTTP "
                + response.statusCode() + "): " + brief(response.body()));
    }

    private boolean indexExists(String indexName) {
        HttpRequest.Builder builder = HttpRequest.newBuilder(indexUri(indexName))
                .timeout(REQUEST_TIMEOUT)
                .method("HEAD", HttpRequest.BodyPublishers.noBody());
        addBasicAuth(builder);

        HttpResponse<String> response = send(builder.build());
        if (response.statusCode() == 200) {
            return true;
        }
        if (response.statusCode() == 404) {
            return false;
        }
        if (response.statusCode() == 401 || response.statusCode() == 403) {
            throw new EsOperationException(503, ES_AUTH_FAILED_MESSAGE);
        }
        throw new IllegalStateException("Elasticsearch index exists check failed (HTTP "
                + response.statusCode() + ")");
    }

    private void createIndex(String indexName) {
        String body = createIndexBody();
        HttpRequest.Builder builder = HttpRequest.newBuilder(indexUri(indexName))
                .timeout(REQUEST_TIMEOUT)
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8));
        addBasicAuth(builder);

        HttpResponse<String> response = send(builder.build());
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return;
        }
        if (response.statusCode() == 400 && response.body() != null
                && response.body().contains("resource_already_exists_exception")) {
            return;
        }
        if (response.statusCode() == 401 || response.statusCode() == 403) {
            throw new EsOperationException(503, ES_AUTH_FAILED_MESSAGE);
        }
        throw new IllegalStateException("Elasticsearch create index failed (HTTP "
                + response.statusCode() + "): " + brief(response.body()));
    }

    private String createIndexBody() {
        Map<String, Object> root = new LinkedHashMap<>();
        Map<String, Object> mappings = new LinkedHashMap<>();
        Map<String, Object> fields = new LinkedHashMap<>();

        fields.put("id", type("long"));
        fields.put("group_code", type("keyword"));
        fields.put("document_id", type("long"));
        fields.put("article_no", type("keyword"));
        fields.put("section_title", textWithKeyword());
        fields.put("topic_tags", type("keyword"));
        fields.put("content", type("text"));
        fields.put("content_vector", denseVectorField());
        fields.put("source_title", textWithKeyword());
        fields.put("source_url", type("keyword"));
        fields.put("authority_level", type("keyword"));
        fields.put("effective_status", type("keyword"));
        fields.put("created_at", dateField());

        mappings.put("properties", fields);
        root.put("mappings", mappings);

        try {
            return objectMapper.writeValueAsString(root);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to build Elasticsearch index mapping", ex);
        }
    }

    private Map<String, Object> denseVectorField() {
        Map<String, Object> field = new LinkedHashMap<>();
        field.put("type", "dense_vector");
        field.put("dims", contentVectorDimension());
        field.put("index", true);
        field.put("similarity", "cosine");
        return field;
    }

    private Map<String, Object> textWithKeyword() {
        Map<String, Object> field = new LinkedHashMap<>();
        field.put("type", "text");
        field.put("fields", Map.of("keyword", Map.of(
                "type", "keyword",
                "ignore_above", 256
        )));
        return field;
    }

    private Map<String, Object> dateField() {
        Map<String, Object> field = new LinkedHashMap<>();
        field.put("type", "date");
        field.put("format", "strict_date_optional_time||epoch_millis");
        return field;
    }

    private Map<String, Object> type(String type) {
        return Map.of("type", type);
    }

    private URI indexUri(String indexName) {
        RagAiProperties.Elasticsearch elasticsearch = properties.getElasticsearch();
        if (elasticsearch == null || !StringUtils.hasText(elasticsearch.getUrl())) {
            throw new EsOperationException(503,
                    "Elasticsearch URL\u672a\u914d\u7f6e\uff0c\u8bf7\u68c0\u67e5RAG_AI_ELASTICSEARCH_URL");
        }
        String baseUrl = elasticsearch.getUrl().trim().replaceAll("/+$", "");
        return URI.create(baseUrl + "/" + encodePathSegment(indexName));
    }

    private URI indexDocumentUri(String indexName, String documentId) {
        return URI.create(indexUri(indexName).toString() + INDEX_DOC_PATH + encodePathSegment(documentId));
    }

    private URI embeddingUri() {
        RagAiProperties.ModelEndpoint embedding = properties.getEmbedding();
        if (embedding == null || !StringUtils.hasText(embedding.getBaseUrl())) {
            throw new EsOperationException(503,
                    "Embedding URL\u672a\u914d\u7f6e\uff0c\u8bf7\u68c0\u67e5RAG_AI_EMBEDDING_BASE_URL");
        }
        String baseUrl = embedding.getBaseUrl().trim().replaceAll("/+$", "");
        return URI.create(baseUrl + EMBEDDING_PATH);
    }

    private String normalizeIndexName(String indexName) {
        if (StringUtils.hasText(indexName)) {
            return indexName.trim();
        }
        return DEFAULT_INDEX_NAME;
    }

    private String encodePathSegment(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }

    private void addBasicAuth(HttpRequest.Builder builder) {
        RagAiProperties.Elasticsearch elasticsearch = properties.getElasticsearch();
        if (elasticsearch == null
                || !StringUtils.hasText(elasticsearch.getUsername())
                || !StringUtils.hasText(elasticsearch.getPassword())) {
            return;
        }
        String raw = elasticsearch.getUsername().trim() + ":" + elasticsearch.getPassword().trim();
        String encoded = Base64.getEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
        builder.header("Authorization", "Basic " + encoded);
    }

    private void addBearerAuth(HttpRequest.Builder builder, RagAiProperties.ModelEndpoint endpoint) {
        if (endpoint == null || !StringUtils.hasText(endpoint.getApiKey())) {
            return;
        }
        builder.header("Authorization", "Bearer " + endpoint.getApiKey().trim());
    }

    private HttpResponse<String> send(HttpRequest request) {
        try {
            return httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        } catch (Exception ex) {
            throw new IllegalStateException("Remote request failed: " + rootMessage(ex), ex);
        }
    }

    private String brief(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        String normalized = value.replaceAll("\\s+", " ").trim();
        return normalized.length() <= 300 ? normalized : normalized.substring(0, 300);
    }

    private String defaultText(String value, String defaultValue) {
        return StringUtils.hasText(value) ? value : defaultValue;
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to write JSON request body", ex);
        }
    }

    private List<Double> readEmbedding(String body) {
        try {
            List<Double> vector = new ArrayList<>();
            com.fasterxml.jackson.databind.JsonNode root = objectMapper.readTree(body);
            com.fasterxml.jackson.databind.JsonNode embeddings = root.path("data").path(0).path("embedding");
            if (!embeddings.isArray()) {
                return List.of();
            }
            for (com.fasterxml.jackson.databind.JsonNode node : embeddings) {
                vector.add(node.asDouble());
            }
            return List.copyOf(vector);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to parse embedding response", ex);
        }
    }

    private String rootMessage(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null) {
            current = current.getCause();
        }
        return current.getMessage() == null ? throwable.getClass().getSimpleName() : current.getMessage();
    }

    private boolean isSectionHeading(String text) {
        return SECTION_HEADING_PATTERN.matcher(text).matches();
    }

    private void appendParagraph(StringBuilder content, String paragraph) {
        if (content.length() > 0) {
            content.append('\n');
        }
        content.append(paragraph);
    }

    private void addDraft(List<ChunkDraft> drafts, String articleNo, String sectionTitle, String content) {
        if (StringUtils.hasText(content)) {
            drafts.add(new ChunkDraft(articleNo, sectionTitle, content.trim()));
        }
    }

    private List<LegalKnowledgeChunk> toChunks(List<ChunkDraft> drafts) {
        List<LegalKnowledgeChunk> chunks = new ArrayList<>();
        int chunkIndex = 0;
        for (ChunkDraft draft : drafts) {
            for (String contentPart : splitLongContent(draft.content())) {
                chunks.add(new LegalKnowledgeChunk(
                        chunkIndex,
                        draft.articleNo(),
                        draft.sectionTitle(),
                        List.of(),
                        contentPart
                ));
                chunkIndex++;
            }
        }
        return List.copyOf(chunks);
    }

    private List<String> splitLongContent(String content) {
        String trimmed = content == null ? "" : content.trim();
        if (!StringUtils.hasText(trimmed)) {
            return List.of();
        }
        if (trimmed.length() <= CHUNK_MAX_LENGTH) {
            return List.of(trimmed);
        }

        List<String> parts = new ArrayList<>();
        int start = 0;
        while (start < trimmed.length()) {
            int remaining = trimmed.length() - start;
            if (remaining <= CHUNK_MAX_LENGTH) {
                addContentPart(parts, trimmed.substring(start));
                break;
            }

            int minEnd = Math.min(start + CHUNK_MIN_LENGTH, trimmed.length());
            int maxEnd = Math.min(start + CHUNK_MAX_LENGTH, trimmed.length());
            int splitAt = findSplitBoundary(trimmed, minEnd, maxEnd);
            addContentPart(parts, trimmed.substring(start, splitAt));
            start = splitAt;
            while (start < trimmed.length() && Character.isWhitespace(trimmed.charAt(start))) {
                start++;
            }
        }
        return List.copyOf(parts);
    }

    private int findSplitBoundary(String text, int minEnd, int maxEnd) {
        for (int index = maxEnd - 1; index >= minEnd; index--) {
            char value = text.charAt(index);
            if (value == '\u3002' || value == '\uff1b' || value == ';'
                    || value == '\uff01' || value == '\uff1f' || value == '\n') {
                return index + 1;
            }
        }
        return maxEnd;
    }

    private void addContentPart(List<String> parts, String content) {
        String trimmed = content.trim();
        if (StringUtils.hasText(trimmed)) {
            parts.add(trimmed);
        }
    }

    private record ChunkDraft(String articleNo, String sectionTitle, String content) {
    }

    public record LegalKnowledgeChunk(
            int chunkIndex,
            String articleNo,
            String sectionTitle,
            List<String> topicTags,
            String content) {

        public LegalKnowledgeChunk {
            topicTags = topicTags == null ? List.of() : List.copyOf(topicTags);
            content = content == null ? "" : content.trim();
        }
    }
}
