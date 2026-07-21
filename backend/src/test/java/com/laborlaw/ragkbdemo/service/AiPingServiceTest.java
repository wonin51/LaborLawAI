package com.laborlaw.ragkbdemo.service;

import com.laborlaw.ragkbdemo.config.RagAiProperties;
import com.laborlaw.ragkbdemo.vo.AiPingVO;
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

import static org.assertj.core.api.Assertions.assertThat;

class AiPingServiceTest {

    private HttpServer server;
    private final List<CapturedRequest> requests = new ArrayList<>();

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void pingCallsConfiguredElasticsearchEmbeddingAndChatEndpoints() throws Exception {
        startServer();
        RagAiProperties properties = properties(baseUrl());
        AiPingService service = new AiPingService(properties, HttpClient.newHttpClient());

        AiPingVO result = service.ping();

        assertThat(result.elasticsearch().success()).isTrue();
        assertThat(result.embedding().success()).isTrue();
        assertThat(result.chat().success()).isTrue();
        assertThat(requests).extracting(CapturedRequest::path)
                .containsExactly("/es/", "/v1/embeddings", "/v1/chat/completions");
        assertThat(requests.get(1).body()).contains("\"model\":\"qwen3-embedding:4b\"");
        assertThat(requests.get(2).body()).contains("\"model\":\"qwen3:4b\"");
    }

    @Test
    void pingReturnsClearErrorForFailedComponentWithoutFailingWholeResponse() throws Exception {
        startServerWithChatFailure();
        AiPingService service = new AiPingService(properties(baseUrl()), HttpClient.newHttpClient());

        AiPingVO result = service.ping();

        assertThat(result.elasticsearch().success()).isTrue();
        assertThat(result.embedding().success()).isTrue();
        assertThat(result.chat().success()).isFalse();
        assertThat(result.chat().message()).isEqualTo("Chat returned HTTP 503");
    }

    private void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/es/", exchange -> respond(exchange, 200, "{\"cluster_name\":\"test\"}"));
        server.createContext("/v1/embeddings", exchange -> respond(exchange, 200, "{\"data\":[{\"embedding\":[0.1]}]}"));
        server.createContext("/v1/chat/completions", exchange -> respond(exchange, 200, "{\"choices\":[{\"message\":{\"content\":\"pong\"}}]}"));
        server.start();
    }

    private void startServerWithChatFailure() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/es/", exchange -> respond(exchange, 200, "{}"));
        server.createContext("/v1/embeddings", exchange -> respond(exchange, 200, "{}"));
        server.createContext("/v1/chat/completions", exchange -> respond(exchange, 503, "chat unavailable"));
        server.start();
    }

    private void respond(HttpExchange exchange, int status, String body) throws IOException {
        byte[] requestBody = exchange.getRequestBody().readAllBytes();
        requests.add(new CapturedRequest(
                exchange.getRequestURI().getPath(),
                exchange.getRequestMethod(),
                new String(requestBody, StandardCharsets.UTF_8)
        ));
        byte[] responseBody = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(status, responseBody.length);
        exchange.getResponseBody().write(responseBody);
        exchange.close();
    }

    private RagAiProperties properties(String baseUrl) {
        RagAiProperties properties = new RagAiProperties();
        RagAiProperties.Elasticsearch elasticsearch = new RagAiProperties.Elasticsearch();
        elasticsearch.setUrl(baseUrl + "/es/");
        properties.setElasticsearch(elasticsearch);

        RagAiProperties.ModelEndpoint embedding = new RagAiProperties.ModelEndpoint();
        embedding.setBaseUrl(baseUrl + "/v1");
        embedding.setModel("qwen3-embedding:4b");
        embedding.setDimension(2560);
        properties.setEmbedding(embedding);

        RagAiProperties.ModelEndpoint chat = new RagAiProperties.ModelEndpoint();
        chat.setBaseUrl(baseUrl + "/v1");
        chat.setModel("qwen3:4b");
        properties.setChat(chat);
        return properties;
    }

    private String baseUrl() {
        return "http://127.0.0.1:" + server.getAddress().getPort();
    }

    private record CapturedRequest(String path, String method, String body) {
    }
}
