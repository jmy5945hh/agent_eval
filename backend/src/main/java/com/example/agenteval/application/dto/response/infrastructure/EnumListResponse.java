package com.example.agenteval.application.dto.response.infrastructure;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ApiModel("枚举列表返回体")
public class EnumListResponse {

    @ApiModelProperty(value = "主键id")
    private Integer id;

    @ApiModelProperty(value = "枚举key")
    private String enumKey;

    @ApiModelProperty(value = "枚举value")
    private String enumValue;

}
