package com.laborlaw.ragkbdemo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("qa_answer")
public class QaAnswer {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long messageId;
    private Long questionMessageId;
    private Long topicId;
    private String answerStatus;
    private String conclusionSummary;
    private String actionSteps;
    private String evidenceChecklist;
    private String cautions;
    private String refusalCode;
    private String disclaimerText;
    private BigDecimal citationCoverage;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
