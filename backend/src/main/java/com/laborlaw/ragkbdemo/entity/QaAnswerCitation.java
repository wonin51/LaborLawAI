package com.laborlaw.ragkbdemo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("qa_answer_citation")
public class QaAnswerCitation {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long claimId;
    private Long documentVersionId;
    private Long chunkRefId;
    private Integer citationNo;
    private Integer retrievalRank;
    private BigDecimal relevanceScore;
    private String sourceTitleSnapshot;
    private String sourceLabelSnapshot;
    private String sectionSnapshot;
    private String articleNoSnapshot;
    private String sourceUrlSnapshot;
    private String quoteSnapshot;
    private LocalDateTime createdAt;
}
