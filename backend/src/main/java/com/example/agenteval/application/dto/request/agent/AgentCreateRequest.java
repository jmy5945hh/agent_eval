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
@ApiModel("创建Agent请求体")
public class AgentCreateRequest {
    /**
     * Agent名称
     */
    @NotBlank(message = "Agent 名称不能为空")
    @ApiModelProperty(value = "Agent名称", required = true)
    private String agentName;

    /**
     * 描述
     */
    @ApiModelProperty(value = "描述")
    private String description;
    /**
     * 启动命令
     */
    @NotBlank(message = "启动命令不能为空")
    @ApiModelProperty(value = "启动命令", required = true)
    private String startCmd;

    /**
     * 启用状态:true-启用
     */
    @ApiModelProperty(value = "启用状态:true-启用,false禁用，默认:true")
    private Boolean enabled;


    /**
     * 配置文件路径
     */
    @ApiModelProperty(value = "配置文件路径", required = true)
    @NotBlank(message = "配置文件路径不能为空")
    private String configPath;

    /**
     * 是否默认agent
     */
    @ApiModelProperty(value = "是否默认agent")
    private Boolean defaultAgent;


}

