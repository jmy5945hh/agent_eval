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
@ApiModel("修改Agent基本信息请求体")
public class AgentUpdateRequest {

    /**
     * Agent名称
     */
    @ApiModelProperty(value = "Agent名称")
    private String agentName;

    /**
     * 描述
     */
    @ApiModelProperty(value = "描述")
    private String description;
    /**
     * 启动命令
     */
    @ApiModelProperty(value = "启动命令")
    private String startCmd;

    /**
     * 版本启用状态:true-启用
     */
    @ApiModelProperty(value = "版本启用状态:true-启用,false-禁用，默认:true")
    private Boolean enabled = true;

    /**
     * 配置文件路径
     */
    @ApiModelProperty(value = "配置文件内容(模型配置格式)")
    private String configPath;

    /**
     * 默认agent
     */
    @ApiModelProperty(value = "是否默认版本：true-是，false-否")
    private Boolean defaultAgent;
}
