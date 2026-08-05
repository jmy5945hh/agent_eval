package com.example.agenteval.application.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ApiModel("分页请求参数")
public class BasePageRequest {

    /**
     * 页码，后端从0开始
     */
    @ApiModelProperty(value = "页码，JPA使用的页码从0开始")
    private int page = 0;

    /**
     * 每页大小
     */
    @ApiModelProperty(value = "每页大小")
    private int size = 10;

}
