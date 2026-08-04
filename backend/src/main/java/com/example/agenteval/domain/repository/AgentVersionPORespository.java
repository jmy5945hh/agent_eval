package com.example.agenteval.domain.repository;

import com.example.agenteval.domain.model.AgentVersionPO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AgentVersionPORespository extends BaseRepository<AgentVersionPO, Integer> {

    /**
     * 查询指定 Agent 下的所有版本。
     */
    List<AgentVersionPO> findByAgentId(int agentId);

    /**
     * 判断指定 Agent 下某个版本号是否已存在（版本号在 Agent 下唯一）。
     */
    boolean existsByAgentIdAndVersion(int agentId, String version);

    /**
     * 根据agentId分页查询
     *
     * @param agentId
     * @param pageable
     * @return
     */
    Page<AgentVersionPO> findByAgentId(Integer agentId, Pageable pageable);
}
