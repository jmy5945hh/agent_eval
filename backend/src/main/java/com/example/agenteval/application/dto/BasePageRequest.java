package com.example.agenteval.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class BasePageRequest {

    /**
     * 页码，后端从0开始
     */
    private int page = 0;

    /**
     * 每页大小
     */
    private int size = 10;

}
