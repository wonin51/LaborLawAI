package com.ailaw.ragkbdemo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Health check response for the EOS repair work order RAG knowledge base demo backend.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HealthVO {

    private String status;
}
