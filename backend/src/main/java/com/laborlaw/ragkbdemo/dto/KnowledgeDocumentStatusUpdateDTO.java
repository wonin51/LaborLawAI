package com.laborlaw.ragkbdemo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class KnowledgeDocumentStatusUpdateDTO {

    @NotBlank(message = "status is required")
    private String status;
}
