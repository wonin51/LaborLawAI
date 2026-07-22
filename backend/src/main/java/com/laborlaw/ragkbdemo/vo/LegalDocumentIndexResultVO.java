package com.laborlaw.ragkbdemo.vo;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LegalDocumentIndexResultVO {

    @JsonProperty("document_id")
    private Long documentId;

    @JsonProperty("chunk_count")
    private Integer chunkCount;

    @JsonProperty("indexed_count")
    private Integer indexedCount;

    @JsonProperty("failed_count")
    private Integer failedCount;

    @JsonProperty("status")
    private String status;

    public Long getDocumentId() {
        return documentId;
    }

    public void setDocumentId(Long documentId) {
        this.documentId = documentId;
    }

    public Integer getChunkCount() {
        return chunkCount;
    }

    public void setChunkCount(Integer chunkCount) {
        this.chunkCount = chunkCount;
    }

    public Integer getIndexedCount() {
        return indexedCount;
    }

    public void setIndexedCount(Integer indexedCount) {
        this.indexedCount = indexedCount;
    }

    public Integer getFailedCount() {
        return failedCount;
    }

    public void setFailedCount(Integer failedCount) {
        this.failedCount = failedCount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
