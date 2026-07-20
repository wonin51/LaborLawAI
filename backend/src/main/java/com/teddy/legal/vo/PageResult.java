package com.teddy.legal.vo;

import java.util.List;

public record PageResult<T>(List<T> list, long total, int pageNo, int pageSize) {
}
