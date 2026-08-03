package com.example.agenteval.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {
    private List<T> list;
    private long total;
    private int page;
    private int size;

    public static <T> PageResponse<T> of(List<T> list, long total, int page, int size) {
        return PageResponse.<T>builder()
                .list(list)
                .total(total)
                .page(page)
                .size(size)
                .build();
    }
}
