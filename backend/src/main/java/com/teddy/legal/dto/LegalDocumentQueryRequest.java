package com.teddy.legal.dto;

import jakarta.validation.constraints.Min;

public record LegalDocumentQueryRequest(
        String title,
        String sourceType,
        String jurisdiction,
        String effectiveStatus,
        @Min(1) Integer pageNo,
        @Min(1) Integer pageSize
) {
}
