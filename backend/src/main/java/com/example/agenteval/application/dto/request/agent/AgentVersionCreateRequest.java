package com.example.agenteval.application.dto.request.agent;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ApiModel("创建Agent版本请求体")
public class AgentVersionCreateRequest {

    @ApiModelProperty(value = "Agent版本")
    @NotBlank(message = "版本号不能为空")
    private String version;
    /**
     * 版本说明
     */
    @ApiModelProperty(value = "版本说明")
    private String notes;

    /**
     * 启用状态
     */
    @ApiModelProperty(value = "启用状态：true-启用，false-禁用，默认：true")
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
