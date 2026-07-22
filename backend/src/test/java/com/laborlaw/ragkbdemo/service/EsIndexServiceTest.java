package com.laborlaw.ragkbdemo.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.laborlaw.ragkbdemo.config.RagAiProperties;
import com.laborlaw.ragkbdemo.exception.EsOperationException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EsIndexServiceTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final List<CapturedRequest> requests = new ArrayList<>();
    private HttpServer server;

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void initIndexCreatesLegalChunkMappingWithCappedDenseVectorDimension() throws Exception {
        startServer(false);
        EsIndexService service = new EsIndexService(properties(baseUrl(), 2560), objectMapper, HttpClient.newHttpClient());

        String result = service.initIndex(null);

        assertThat(result).isEqualTo("\u7d22\u5f15\u521b\u5efa\u6210\u529f");
        assertThat(service.contentVectorDimension()).isEqualTo(2048);
        assertThat(requests).extracting(CapturedRequest::method).containsExactly("HEAD", "PUT");
        assertThat(requests).extracting(CapturedRequest::path)
                .containsExactly("/es/legal_chunk_index_g5", "/es/legal_chunk_index_g5");

        JsonNode body = objectMapper.readTree(requests.get(1).body());
        JsonNode vector = body.at("/mappings/properties/content_vector");
        assertThat(vector.get("type").asText()).isEqualTo("dense_vector");
        assertThat(vector.get("dims").asInt()).isEqualTo(2048);
        assertThat(vector.get("similarity").asText()).isEqualTo("cosine");
        assertThat(body.at("/mappings/properties/group_code/type").asText()).isEqualTo("keyword");
        assertThat(body.at("/mappings/properties/content/type").asText()).isEqualTo("text");
        assertThat(body.at("/mappings/properties/created_at/type").asText()).isEqualTo("date");
    }

    @Test
    void initIndexReturnsExistsMessageWithoutCreatingWhenIndexAlreadyExists() throws Exception {
        startServer(true);
        EsIndexService service = new EsIndexService(properties(baseUrl(), 1024), objectMapper, HttpClient.newHttpClient());

        String result = service.initIndex("existing_index");

        assertThat(result).isEqualTo("\u7d22\u5f15\u5df2\u5b58\u5728");
        assertThat(service.contentVectorDimension()).isEqualTo(1024);
        assertThat(requests).extracting(CapturedRequest::method).containsExactly("HEAD");
        assertThat(requests.get(0).path()).isEqualTo("/es/existing_index");
    }

    @Test
    void initIndexReturnsClearFailureWhenElasticsearchAuthenticationFails() throws Exception {
        startServerWithStatus(401);
        EsIndexService service = new EsIndexService(properties(baseUrl(), 1024), objectMapper, HttpClient.newHttpClient());

        assertThatThrownBy(() -> service.initIndex("auth_failed_index"))
                .isInstanceOf(EsOperationException.class)
                .hasMessage("Elasticsearch\u8ba4\u8bc1\u5931\u8d25\uff0c\u8bf7\u68c0\u67e5RAG_AI_ELASTICSEARCH_USERNAME/RAG_AI_ELASTICSEARCH_PASSWORD");
        assertThat(requests).extracting(CapturedRequest::method).containsExactly("HEAD");
    }

    @Test
    void embedTextUsesEmbeddingEndpointAndParsesVector() throws Exception {
        startServer(false);
        RagAiProperties properties = properties(baseUrl(), 2560);
        RagAiProperties.ModelEndpoint embedding = properties.getEmbedding();
        embedding.setBaseUrl(baseUrl() + "/v1");
        embedding.setApiKey("test-key");
        EsIndexService service = new EsIndexService(properties, objectMapper, HttpClient.newHttpClient());

        List<Double> vector = service.embedText("测试内容");

        assertThat(vector).containsExactly(1.0, 2.0, 3.0);
        assertThat(requests).extracting(CapturedRequest::method).contains("POST");
        assertThat(requests).extracting(CapturedRequest::path).contains("/v1/embeddings");
        assertThat(requests.get(requests.size() - 1).authorization()).isEqualTo("Bearer test-key");
    }

    @Test
    void indexDocumentWritesPayloadToEsDocumentEndpoint() throws Exception {
        startServer(false);
        EsIndexService service = new EsIndexService(properties(baseUrl(), 2560), objectMapper, HttpClient.newHttpClient());

        String result = service.indexDocument("legal_chunk_index_g5", "123", Map.of("content", "示例"));

        assertThat(result).isEqualTo("123");
        assertThat(requests).extracting(CapturedRequest::method).contains("PUT");
        assertThat(requests).extracting(CapturedRequest::path)
                .contains("/es/legal_chunk_index_g5/_doc/123");
    }

    @Test
    void splitLegalDocumentKeepsArticleSectionAndNaturalParagraphMetadata() {
        EsIndexService service = new EsIndexService(new RagAiProperties(), objectMapper, HttpClient.newHttpClient());
        String longParagraph = "\u7532".repeat(1300);
        String rawText = String.join("\n\n",
                "\u7b2c\u4e00\u7ae0 \u603b\u5219",
                "\u7b2c\u4e00\u6761 \u4e3a\u4e86\u4fdd\u62a4\u52b3\u52a8\u8005\u5408\u6cd5\u6743\u76ca\u3002\n"
                        + "\u7528\u4eba\u5355\u4f4d\u5e94\u5f53\u4f9d\u6cd5\u5efa\u7acb\u52b3\u52a8\u5173\u7cfb\u3002",
                "\u7b2c\u4e8c\u6761 \u4e2d\u534e\u4eba\u6c11\u5171\u548c\u56fd\u5883\u5185\u7684\u4f01\u4e1a\u9002\u7528\u672c\u6cd5\u3002",
                "\u7b2c\u4e8c\u7ae0 \u9644\u5219",
                "   ",
                "\u8fd9\u662f\u6ca1\u6709\u6761\u6b3e\u53f7\u7684\u81ea\u7136\u6bb5\u3002",
                longParagraph
        );

        List<EsIndexService.LegalKnowledgeChunk> chunks = service.splitLegalDocument(rawText);

        assertThat(chunks).hasSize(5);
        assertThat(chunks).extracting(EsIndexService.LegalKnowledgeChunk::chunkIndex)
                .containsExactly(0, 1, 2, 3, 4);
        assertThat(chunks.get(0).articleNo()).isEqualTo("\u7b2c\u4e00\u6761");
        assertThat(chunks.get(0).sectionTitle()).isEqualTo("\u7b2c\u4e00\u7ae0 \u603b\u5219");
        assertThat(chunks.get(0).content()).contains("\u7528\u4eba\u5355\u4f4d");
        assertThat(chunks.get(1).articleNo()).isEqualTo("\u7b2c\u4e8c\u6761");
        assertThat(chunks.get(2).articleNo()).isNull();
        assertThat(chunks.get(2).sectionTitle()).isEqualTo("\u7b2c\u4e8c\u7ae0 \u9644\u5219");
        assertThat(chunks.get(2).content()).isEqualTo("\u8fd9\u662f\u6ca1\u6709\u6761\u6b3e\u53f7\u7684\u81ea\u7136\u6bb5\u3002");
        assertThat(chunks).allSatisfy(chunk -> {
            assertThat(chunk.content()).isNotBlank();
            assertThat(chunk.content().length()).isLessThanOrEqualTo(1200);
            assertThat(chunk.topicTags()).isEmpty();
        });
    }

    private void startServer(boolean indexExists) throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/es/", exchange -> {
            byte[] requestBody = exchange.getRequestBody().readAllBytes();
            requests.add(new CapturedRequest(
                    exchange.getRequestURI().getPath(),
                    exchange.getRequestMethod(),
                    new String(requestBody, StandardCharsets.UTF_8),
                    exchange.getRequestHeaders().getFirst("Authorization")
            ));

            if ("HEAD".equals(exchange.getRequestMethod())) {
                respondHead(exchange, indexExists ? 200 : 404);
                return;
            }
            if ("PUT".equals(exchange.getRequestMethod())) {
                respond(exchange, 200, "{\"acknowledged\":true}");
                return;
            }
            respond(exchange, 405, "{}");
        });
        server.createContext("/v1/embeddings", exchange -> {
            byte[] requestBody = exchange.getRequestBody().readAllBytes();
            requests.add(new CapturedRequest(
                    exchange.getRequestURI().getPath(),
                    exchange.getRequestMethod(),
                    new String(requestBody, StandardCharsets.UTF_8),
                    exchange.getRequestHeaders().getFirst("Authorization")
            ));
            if ("POST".equals(exchange.getRequestMethod())) {
                respond(exchange, 200, "{\"data\":[{\"embedding\":[1.0,2.0,3.0]}]}");
                return;
            }
            respond(exchange, 405, "{}");
        });
        server.start();
    }

    private void startServerWithStatus(int status) throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/es/", exchange -> {
            byte[] requestBody = exchange.getRequestBody().readAllBytes();
            requests.add(new CapturedRequest(
                    exchange.getRequestURI().getPath(),
                    exchange.getRequestMethod(),
                    new String(requestBody, StandardCharsets.UTF_8),
                    exchange.getRequestHeaders().getFirst("Authorization")
            ));
            if ("HEAD".equals(exchange.getRequestMethod())) {
                respondHead(exchange, status);
                return;
            }
            respond(exchange, status, "{}");
        });
        server.createContext("/v1/embeddings", exchange -> {
            byte[] requestBody = exchange.getRequestBody().readAllBytes();
            requests.add(new CapturedRequest(
                    exchange.getRequestURI().getPath(),
                    exchange.getRequestMethod(),
                    new String(requestBody, StandardCharsets.UTF_8),
                    exchange.getRequestHeaders().getFirst("Authorization")
            ));
            respond(exchange, status, "{}");
        });
        server.start();
    }

    private void respondHead(HttpExchange exchange, int status) throws IOException {
        exchange.sendResponseHeaders(status, -1);
        exchange.close();
    }

    private void respond(HttpExchange exchange, int status, String body) throws IOException {
        byte[] responseBody = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(status, responseBody.length);
        exchange.getResponseBody().write(responseBody);
        exchange.close();
    }

    private RagAiProperties properties(String baseUrl, int dimension) {
        RagAiProperties properties = new RagAiProperties();
        RagAiProperties.Elasticsearch elasticsearch = new RagAiProperties.Elasticsearch();
        elasticsearch.setUrl(baseUrl + "/es/");
        properties.setElasticsearch(elasticsearch);

        RagAiProperties.ModelEndpoint embedding = new RagAiProperties.ModelEndpoint();
        embedding.setDimension(dimension);
        properties.setEmbedding(embedding);
        return properties;
    }

    private String baseUrl() {
        return "http://127.0.0.1:" + server.getAddress().getPort();
    }

    private record CapturedRequest(String path, String method, String body, String authorization) {
    }
}
