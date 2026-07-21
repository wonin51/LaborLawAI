package com.laborlaw.ragkbdemo.dto;

import lombok.Data;

@Data
public class KbChunkRefQueryDTO {

    private Long documentVersionId;
    private String status;
    private String chunkKey;
    private String sectionPath;
    private Long pageNo = 1L;
    private Long pageSize = 10L;
}
