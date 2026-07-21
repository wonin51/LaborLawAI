package com.laborlaw.ragkbdemo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "rag.ai")
public class RagAiProperties {

    private Elasticsearch elasticsearch = new Elasticsearch();
    private ModelEndpoint embedding = new ModelEndpoint();
    private ModelEndpoint chat = new ModelEndpoint();

    public Elasticsearch getElasticsearch() {
        return elasticsearch;
    }

    public void setElasticsearch(Elasticsearch elasticsearch) {
        this.elasticsearch = elasticsearch;
    }

    public ModelEndpoint getEmbedding() {
        return embedding;
    }

    public void setEmbedding(ModelEndpoint embedding) {
        this.embedding = embedding;
    }

    public ModelEndpoint getChat() {
        return chat;
    }

    public void setChat(ModelEndpoint chat) {
        this.chat = chat;
    }

    public static class Elasticsearch {
        private String url;
        private String username;
        private String password;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    public static class ModelEndpoint {
        private String baseUrl;
        private String apiKey;
        private String model;
        private Integer dimension;

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }

        public Integer getDimension() {
            return dimension;
        }

        public void setDimension(Integer dimension) {
            this.dimension = dimension;
        }
    }
}
