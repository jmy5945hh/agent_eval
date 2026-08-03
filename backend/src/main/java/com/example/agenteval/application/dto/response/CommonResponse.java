package com.example.agenteval.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommonResponse<T> {

    private Integer code;
    private String message;
    private T data;

    public static <T> CommonResponse<T> success(T data) {
        return CommonResponse.<T>builder()
                .code(200)
                .message("success")
                .data(data)
                .build();
    }

    public static CommonResponse success() {
        return CommonResponse.builder()
                .code(200)
                .message("success")
                .data("")
                .build();
    }

    public static <T> CommonResponse<T> error(Integer code, String message) {
        return CommonResponse.<T>builder()
                .code(code)
                .message(message)
                .build();
    }
}
