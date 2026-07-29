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
@Table(name = "agent_version")
public class AgentVersionPO extends BaseEntity {

    @Column(nullable = true)
    private int agentId;
    /**
     * 版本号（如 v2.3.1）,;
     */
    @Column(nullable = true, length = 20)
    private String version;
    /**
     * 版本说明 / Release Notes,;
     */
    @Column(nullable = true, length = 200)
    private String notes;
    /**
     * 状态: 1-enabled,0-disabled,;
     */
    @Column(nullable = true)
    private byte enabled;
}
