package com.laborlaw.ragkbdemo.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class QaRecordVO {

    private Long id;

    private String question;

    @JsonProperty("conclusion_summary")
    private String conclusionSummary;
    @JsonProperty("answer_status")
    private String answerStatus;
    @JsonProperty("action_steps")
    private String actionSteps;
    @JsonProperty("evidence_checklist")
    private String evidenceChecklist;
    @JsonProperty("cautions")
    private String cautions;
    @JsonProperty("citation_coverage")
    private BigDecimal citationCoverage;
    @JsonProperty("completed_at")
    private LocalDateTime completedAt;
    @JsonProperty("created_at")
    private LocalDateTime createdAt;
    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;
}
