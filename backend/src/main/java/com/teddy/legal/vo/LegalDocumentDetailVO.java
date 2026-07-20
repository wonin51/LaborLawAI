package com.teddy.legal.vo;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record LegalDocumentDetailVO(
        Long id,
        String title,
        String sourceType,
        String jurisdiction,
        LocalDate publishDate,
        String effectiveStatus,
        String sourceUrl,
        String summary,
        String content,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
