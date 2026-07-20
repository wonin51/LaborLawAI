package com.laborlaw.ragkbdemo.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class KnowledgeDocumentVO {

    private Long id;

    @JsonProperty("public_id")
    private String publicId;
    private String title;
    @JsonProperty("document_type")
    private String documentType;
    @JsonProperty("issuing_authority")
    private String issuingAuthority;
    @JsonProperty("canonical_source_url")
    private String canonicalSourceUrl;
    @JsonProperty("jurisdiction_code")
    private String jurisdictionCode;
    @JsonProperty("scope_text")
    private String scopeText;
    @JsonProperty("authority_level")
    private Integer authorityLevel;
    @JsonProperty("current_version_id")
    private Long currentVersionId;
    private String status;
    @JsonProperty("created_by")
    private Long createdBy;
    @JsonProperty("lock_version")
    private Integer lockVersion;
    @JsonProperty("created_at")
    private LocalDateTime createdAt;
    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;
}
