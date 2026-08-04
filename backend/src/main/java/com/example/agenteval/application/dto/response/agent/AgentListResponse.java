package com.example.agenteval.application.dto.response.agent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgentListResponse {

    /**
     * 主键Id
     */
    private Integer id;

    /**
     * agent名称
     */
    private String agentName;

    /**
     * 默认版本
     */
    private String defaultVersion;

    /**
     * 描述
     */
    private String description;

    /**
     * 启动命令
     */
    private String startCmd;

    /**
     * 启用状态
     */
    private boolean enabled;

    /**
     * 配置文件路径
     */
    private String configPath;

    /**
     * 是否默认agent
     */
    private boolean defaultAgent;

    /**
     * 创建时间
     */
    private String createTime;

    /**
     * 更新时间
     */
    private String updateTime;

}
