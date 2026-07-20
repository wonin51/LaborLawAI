package com.laborlaw.ragkbdemo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    /**
     * 业务状态码，0 表示请求处理成功。
     */
    private int code;

    /**
     * 当前请求的结果说明，便于前端展示或排查问题。
     */
    private String message;

    /**
     * 实际响应数据，使用泛型支持不同接口返回不同的数据结构。
     */
    private T data;

    /**
     * 构建统一的成功响应，默认 code 为 0，message 为 success。
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(0, "success", data);
    }

    /**
     * 构建统一的错误响应，可携带错误码、错误说明和可选的响应数据。
     */
    public static <T> ApiResponse<T> error(int code, String message, T data) {
        return new ApiResponse<>(code, message, data);
    }
}
