package com.example.agenteval.domain.model.pojo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "评分维度请求体")
@SuperBuilder
public class ScoringDimension {
    @NotBlank(message = "唯一标识不能为空")
    @ApiModelProperty(value = "唯一标识", required = true)
    private String key;
    @NotBlank(message = "显示名称不能为空")
    @ApiModelProperty(value = "显示名称", required = true)
    private String label;
    @NotNull(message = "权重不能为空")
    @ApiModelProperty(value = "权重", required = true)
    private Integer weight;
    @ApiModelProperty("描述")
    private String desc;
}
