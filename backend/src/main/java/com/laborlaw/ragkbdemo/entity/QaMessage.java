package com.laborlaw.ragkbdemo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("qa_message")
public class QaMessage {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long sessionId;
    private Integer seqNo;
    private String clientRequestId;
    private String role;
    private String contentRedacted;
    private Long detectedTopicId;
    private Boolean piiDetected;
    private String redactionInfo;
    private LocalDateTime createdAt;
}
