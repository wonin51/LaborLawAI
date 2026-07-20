package com.laborlaw.ragkbdemo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageVO<T> {

    private List<T> records;

    private Long total;

    private Long pageNo;

    private Long pageSize;

    private Long pages;
}
