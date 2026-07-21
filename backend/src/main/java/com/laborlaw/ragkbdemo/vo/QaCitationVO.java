package com.laborlaw.ragkbdemo.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class QaCitationVO {

    private Long id;

    @JsonProperty("claim_id")
    private Long claimId;
    @JsonProperty("document_version_id")
    private Long documentVersionId;
    @JsonProperty("chunk_ref_id")
    private Long chunkRefId;
    @JsonProperty("citation_no")
    private Integer citationNo;
    @JsonProperty("retrieval_rank")
    private Integer retrievalRank;
    @JsonProperty("relevance_score")
    private BigDecimal relevanceScore;
    @JsonProperty("source_title_snapshot")
    private String sourceTitleSnapshot;
    @JsonProperty("source_label_snapshot")
    private String sourceLabelSnapshot;
    @JsonProperty("section_snapshot")
    private String sectionSnapshot;
    @JsonProperty("article_no_snapshot")
    private String articleNoSnapshot;
    @JsonProperty("source_url_snapshot")
    private String sourceUrlSnapshot;
    @JsonProperty("quote_snapshot")
    private String quoteSnapshot;
    @JsonProperty("created_at")
    private LocalDateTime createdAt;
}
