package com.example.agenteval.application.dto.response.agent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgentVersionListResponse {
    /**
     * 主键id
     */
    private Integer id;

    /**
     * agentId
     */
    private Integer agentId;

    /**
     * 版本
     */
    private String version;

    /**
     * 版本说明
     */
    private String notes;

    /**
     * 启用开关
     */
    private boolean enabled;

    /**
     * 配置内容
     */
    private String configContent;

    /**
     * 创建时间
     */
    private String createTime;

    /**
     * 更新时间
     */
    private String updateTime;

}
