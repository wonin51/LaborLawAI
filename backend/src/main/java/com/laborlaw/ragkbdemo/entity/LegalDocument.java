package com.laborlaw.ragkbdemo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("legal_document")
public class LegalDocument {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String groupCode;
    private String sourceTitle;
    private String sourceUrl;
    private String docType;
    private String topicTags;
    private String effectiveStatus;
    private String status;
    private String rawText;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
