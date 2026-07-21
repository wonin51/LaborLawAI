package com.laborlaw.ragkbdemo.dto;

import lombok.Data;

@Data
public class KbDocumentQueryDTO {

    private String title;
    private String documentType;
    private String status;
    private Long pageNo = 1L;
    private Long pageSize = 10L;
}
