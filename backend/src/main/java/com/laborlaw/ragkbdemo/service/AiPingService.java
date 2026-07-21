package com.laborlaw.ragkbdemo.service;

import com.laborlaw.ragkbdemo.config.RagAiProperties;
import com.laborlaw.ragkbdemo.vo.AiPingItemVO;
import com.laborlaw.ragkbdemo.vo.AiPingVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;

@Service
public class AiPingService {

    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(15);
    private static final Duration MODEL_REQUEST_TIMEOUT = Duration.ofMinutes(3);

    private final RagAiProperties properties;
    private final HttpClient httpClient;

    @Autowired
    public AiPingService(RagAiProperties properties) {
        this(properties, HttpClient.newBuilder().connectTimeout(REQUEST_TIMEOUT).build());
    }

    AiPingService(RagAiProperties properties, HttpClient httpClient) {
        this.properties = properties;
        this.httpClient = httpClient;
    }

    public AiPingVO ping() {
        return new AiPingVO(
                pingElasticsearch(),
                pingEmbedding(),
                pingChat()
        );
    }

    private AiPingItemVO pingElasticsearch() {
        RagAiProperties.Elasticsearch elasticsearch = properties.getElasticsearch();
        if (elasticsearch == null || !StringUtils.hasText(elasticsearch.getUrl())) {
            return error("Elasticsearch url is not configured");
        }

        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(elasticsearch.getUrl().trim()))
                    .timeout(REQUEST_TIMEOUT)
                    .GET();
            addBasicAuth(builder, elasticsearch.getUsername(), elasticsearch.getPassword());
            HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            return httpResult("Elasticsearch", response.statusCode());
        } catch (Exception ex) {
            return error("Elasticsearch request failed: " + rootMessage(ex));
        }
    }

    private AiPingItemVO pingEmbedding() {
        RagAiProperties.ModelEndpoint embedding = properties.getEmbedding();
        if (embedding == null || !StringUtils.hasText(embedding.getBaseUrl())) {
            return error("Embedding base-url is not configured");
        }
        if (!StringUtils.hasText(embedding.getModel())) {
            return error("Embedding model is not configured");
        }

        String body = "{\"model\":\"" + json(embedding.getModel()) + "\",\"input\":\"ping\"}";
        return postJson("Embedding", joinUrl(embedding.getBaseUrl(), "/embeddings"), embedding.getApiKey(), body, MODEL_REQUEST_TIMEOUT);
    }

    private AiPingItemVO pingChat() {
        RagAiProperties.ModelEndpoint chat = properties.getChat();
        if (chat == null || !StringUtils.hasText(chat.getBaseUrl())) {
            return error("Chat base-url is not configured");
        }
        if (!StringUtils.hasText(chat.getModel())) {
            return error("Chat model is not configured");
        }

        String body = "{\"model\":\"" + json(chat.getModel())
                + "\",\"messages\":[{\"role\":\"user\",\"content\":\"ping\"}],\"stream\":false}";
        return postJson("Chat", joinUrl(chat.getBaseUrl(), "/chat/completions"), chat.getApiKey(), body, MODEL_REQUEST_TIMEOUT);
    }

    private AiPingItemVO postJson(String name, String url, String apiKey, String body, Duration timeout) {
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(url))
                    .timeout(timeout)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body));
            addBearerAuth(builder, apiKey);
            HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            return httpResult(name, response.statusCode());
        } catch (Exception ex) {
            return error(name + " request failed: " + rootMessage(ex));
        }
    }

    private AiPingItemVO httpResult(String name, int statusCode) {
        if (statusCode >= 200 && statusCode < 300) {
            return ok(name + " is reachable");
        }
        if (statusCode == 401 || statusCode == 403) {
            return error(name + " authentication failed (HTTP " + statusCode + "); check username/password or API key");
        }
        return error(name + " returned HTTP " + statusCode);
    }

    private void addBasicAuth(HttpRequest.Builder builder, String username, String password) {
        if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
            return;
        }
        String raw = username.trim() + ":" + password.trim();
        String encoded = Base64.getEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
        builder.header("Authorization", "Basic " + encoded);
    }

    private void addBearerAuth(HttpRequest.Builder builder, String apiKey) {
        if (StringUtils.hasText(apiKey)) {
            builder.header("Authorization", "Bearer " + apiKey.trim());
        }
    }

    private AiPingItemVO ok(String message) {
        return new AiPingItemVO(true, "ok", message);
    }

    private AiPingItemVO error(String message) {
        return new AiPingItemVO(false, "error", message);
    }

    private String joinUrl(String baseUrl, String path) {
        String normalizedBase = baseUrl.trim().replaceAll("/+$", "");
        return normalizedBase + path;
    }

    private String json(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private String rootMessage(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null) {
            current = current.getCause();
        }
        return current.getMessage() == null ? throwable.getClass().getSimpleName() : current.getMessage();
    }
}
