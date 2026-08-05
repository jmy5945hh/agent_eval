package com.example.agenteval.application.dto.response.model;

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
@ApiModel("模型信息返回体")
public class ModelInfoResponse {

    /**
     * 模型名称
     */
    @ApiModelProperty(value = "模型名称")
    private String modelName;
    /**
     * 模型类型:1-mode，2-name
     */
    @ApiModelProperty(value = "模型类型:1-mode，2-name")
    private Integer modelType;
    /**
     * 地址
     */
    @ApiModelProperty(value = "模型地址")
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
    @ApiModelProperty(value = "是否启用")
    private boolean enabled;
    /**
     * 是否为评分模型（true=评分, false=测评）,默认为false
     */
    @ApiModelProperty(value = "是否为评分模型")
    private boolean scoring;
    /**
     * 描述
     */
    @ApiModelProperty(value = "描述")
    private String description;

    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间,格式：yyyy-MM-dd HH:mm:ss")
    private String createTime;

    /**
     * 修改时间
     */
    @ApiModelProperty(value = "修改时间,格式：yyyy-MM-dd HH:mm:ss")
    private String updateTime;
}
