package com.teddy.legal.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record LegalDocumentCreateRequest(
        @NotBlank(message = "文档标题不能为空")
        String title,

        @NotBlank(message = "来源类型不能为空")
        String sourceType,

        @NotBlank(message = "适用地区不能为空")
        String jurisdiction,

        LocalDate publishDate,

        String effectiveStatus,

        String sourceUrl,

        @NotBlank(message = "摘要不能为空")
        String summary,

        @NotBlank(message = "正文不能为空")
        String content
) {
}
