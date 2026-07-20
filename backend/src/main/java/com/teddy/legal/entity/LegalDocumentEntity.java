package com.teddy.legal.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record LegalDocumentEntity(
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
