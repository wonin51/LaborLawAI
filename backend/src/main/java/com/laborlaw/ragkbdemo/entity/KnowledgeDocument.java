package com.laborlaw.ragkbdemo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("kb_document")
public class KnowledgeDocument {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String publicId;
    private String title;
    private String documentType;
    private String issuingAuthority;
    private String canonicalSourceUrl;
    private String jurisdictionCode;
    private String scopeText;
    private Integer authorityLevel;
    private Long currentVersionId;
    private String status;
    private Long createdBy;
    private Integer lockVersion;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
