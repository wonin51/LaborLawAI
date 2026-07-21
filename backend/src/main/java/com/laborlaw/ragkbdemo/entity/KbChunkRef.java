package com.laborlaw.ragkbdemo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("kb_chunk_ref")
public class KbChunkRef {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long documentVersionId;
    private Long ingestionJobId;
    private String chunkKey;
    private Integer chunkNo;
    private String sectionPath;
    private String articleNo;
    private String paragraphNo;
    private String contentSha256;
    private Integer tokenCount;
    private String esIndexName;
    private String esDocumentId;
    private String status;
    private LocalDateTime createdAt;
}
