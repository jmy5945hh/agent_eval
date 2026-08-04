package com.example.agenteval.application.dto.request.agent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgentUpdateRequest {

    /**
     * Agent名称
     */
    private String agentName;

    /**
     * 描述
     */
    private String description;
    /**
     * 启动说明
     */
    private String startCmd;

    /**
     * 版本启用状态:true-启用
     */
    private Boolean enabled = true;

    /**
     * 配置文件路径
     */
    private String configPath;

    /**
     * 默认agent
     */
    private Boolean defaultAgent;
}
