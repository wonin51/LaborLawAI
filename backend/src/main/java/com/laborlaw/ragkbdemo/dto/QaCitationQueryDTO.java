package com.laborlaw.ragkbdemo.dto;

import lombok.Data;

@Data
public class QaCitationQueryDTO {

    private Long claimId;
    private Long documentVersionId;
    private Long chunkRefId;
    private Long pageNo = 1L;
    private Long pageSize = 10L;
}
