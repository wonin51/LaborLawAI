package com.laborlaw.ragkbdemo.dto;

import lombok.Data;

@Data
public class KnowledgeDocumentPageQueryDTO {

    private String title;

    private String documentType;

    private String status;

    private String issuingAuthority;

    private String jurisdictionCode;

    private Long pageNo = 1L;

    private Long pageSize = 10L;
}
