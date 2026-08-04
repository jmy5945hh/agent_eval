package com.example.agenteval.application.dto.request.agent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgentVersionCreateRequest {

    private String version;
    /**
     * 版本说明
     */
    private String notes;

    /**
     * 启动状态
     */
    private Boolean enabled = true;

    /**
     * 配置文件内容在对象存储的路径
     */
    private String configContent;

    /**
     * 是否默认版本
     */
    private Boolean defaultVersion;

}
