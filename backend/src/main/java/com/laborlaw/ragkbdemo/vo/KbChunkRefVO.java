package com.laborlaw.ragkbdemo.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class KbChunkRefVO {

    private Long id;

    @JsonProperty("document_version_id")
    private Long documentVersionId;
    @JsonProperty("chunk_key")
    private String chunkKey;
    @JsonProperty("chunk_no")
    private Integer chunkNo;
    @JsonProperty("section_path")
    private String sectionPath;
    @JsonProperty("article_no")
    private String articleNo;
    @JsonProperty("token_count")
    private Integer tokenCount;
    private String status;
    @JsonProperty("created_at")
    private LocalDateTime createdAt;
}
