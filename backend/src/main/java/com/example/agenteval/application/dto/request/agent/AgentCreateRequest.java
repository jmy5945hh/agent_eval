package com.example.agenteval.application.dto.request.agent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgentCreateRequest {
    /**
     * Agent名称
     */
    @NotBlank(message = "Agent 名称不能为空")
    private String agentName;

    /**
     * 描述
     */
    private String description;
    /**
     * 启动说明
     */
    @NotBlank(message = "启动命令不能为空")
    private String startCmd;

    /**
     * 启用状态:true-启用
     */
    private Boolean enabled;


    /**
     * 配置文件路径
     */
    @NotBlank(message = "配置文件路径不能为空")
    private String configPath;

    /**
     * 是否默认agent
     */
    private Boolean defaultAgent;


}

