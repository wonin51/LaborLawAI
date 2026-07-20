package com.laborlaw.ragkbdemo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class KnowledgeDocumentUpdateDTO {

    @NotBlank(message = "title is required")
    private String title;

    @NotBlank(message = "document_type is required")
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

    private String status;
}
