package com.example.agenteval.domain.model;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "agent_info")
public class AgentInfoPO extends BaseEntity {

    /**
     * Agent 名称（如 Pi Agent）,;
     */
    @Column(nullable = true, length = 30, name = "agent_name")
    private String agentName;
    /**
     * 默认版本号（如 v2.3.1）,;
     */
    @Column(nullable = true, length = 20)
    private String version;
    /**
     * 功能描述,;
     */
    @Column(nullable = true, length = 200)
    private String description;
    /**
     * agent启动命令,;
     */
    @Column(nullable = true, length = 200, name = "start_cmd")
    private String startCmd;
    /**
     * 状态: 1-enabled,0-disabled,;
     */
    @Column(nullable = true)
    private byte enabled;
}
