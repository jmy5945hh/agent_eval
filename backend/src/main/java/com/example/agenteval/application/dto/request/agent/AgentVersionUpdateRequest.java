package com.example.agenteval.application.dto.request.agent;

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
@ApiModel("编辑Agent版本请求体")
public class AgentVersionUpdateRequest {

    /**
     * AGENT版本
     */
    @ApiModelProperty(value = "AGENT版本", required = true)
    private String version;

    /**
     * 版本说明
     */
    @ApiModelProperty(value = "版本说明")
    private String notes;

    /**
     * 版本开关
     */
    @ApiModelProperty(value = "版本开关:true-开启，false-关闭，默认:true")
    private Boolean enabled = true;

    /**
     * 配置文件内容在对象存储的路径
     */
    @ApiModelProperty(value = "配置文件内容(模型配置格式)")
    private String configContent;

    /**
     * 是否默认版本
     */
    @ApiModelProperty(value = "是否默认版本：true-是，false-否")
    private Boolean defaultVersion;
}
