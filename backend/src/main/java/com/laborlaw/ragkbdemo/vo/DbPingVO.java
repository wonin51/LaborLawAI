package com.laborlaw.ragkbdemo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DbPingVO {

    private String timestamp;

    private String status;
}
