package com.laborlaw.ragkbdemo.dto;

import lombok.Data;

@Data
public class QaRecordQueryDTO {

    private String answerStatus;
    private Long topicId;
    private Long pageNo = 1L;
    private Long pageSize = 10L;
}
