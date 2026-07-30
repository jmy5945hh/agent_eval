package com.example.agenteval.domain.repository;

import com.example.agenteval.domain.model.AgentInfoPO;

public interface AgentInfoPORespository extends BaseRepository<AgentInfoPO, Integer> {

    /**
     * 根据 Agent 名称查找，用于名称唯一性校验。
     */
    AgentInfoPO findByAgentName(String agentName);

    /**
     * 判断指定名称的 Agent 是否已存在。
     */
    boolean existsByAgentName(String agentName);
}
