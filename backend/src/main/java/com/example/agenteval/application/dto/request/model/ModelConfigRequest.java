package com.example.agenteval.application.dto.request.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@ApiModel(value = "模型参数对象")
@Data
public class ModelConfigRequest {
    @ApiModelProperty(value = "模型名称")
    @NotBlank(message = "模型名称不能为空")
    private String modelName;
    /**
     * 模型类型:1-mode，2-name
     */
    @Range(min = 1, max = 2)
    @NotNull(message = "模型类型不能为空")
    @ApiModelProperty(value = "模型名称", allowableValues = "1,2")
    private Integer modelType;

    @NotBlank(message = "模型请求地址")
    @ApiModelProperty(value = "模型请求地址")
    private String endpoint;
    /**
     * 鉴权
     */
    @ApiModelProperty(value = "模型鉴权key")
    private String authorization;
    /**
     * 模型版本号
     */
    @ApiModelProperty(value = "模型版本号")
    private String version;

    /**
     * 是否启用,默认为true
     */
    @ApiModelProperty(value = "是否启用,默认true")
    private Boolean enabled = true;
    /**
     * 是否为评分模型（true=评分, false=测评）,默认为false
     */
    @ApiModelProperty(value = "是否为评分模型,默认false")
    private Boolean scoring = false;
    /**
     * 描述
     */
    @ApiModelProperty(value = "描述")
    private String description;

}
