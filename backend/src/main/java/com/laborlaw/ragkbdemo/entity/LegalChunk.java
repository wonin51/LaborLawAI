package com.laborlaw.ragkbdemo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("legal_chunk")
public class LegalChunk {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String groupCode;
    private Long documentId;
    private Integer chunkIndex;
    private String articleNo;
    private String sectionTitle;
    private String topicTags;
    private String content;
    private String contentHash;
    private String sourceUrl;
    private String authorityLevel;
    private String effectiveStatus;
    private String status;
    private String esDocId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
