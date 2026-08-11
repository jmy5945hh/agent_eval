package com.example.agenteval.domain.repository;

import com.example.agenteval.domain.model.AgentVersionPO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

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

    /**
     * 根据agentIds批量查询
     *
     * @param agentIds
     * @return
     */
    List<AgentVersionPO> findByAgentIdIn(List<Integer> agentIds);

    /**
     * 查询所有可用的Agent版本
     */
    List<AgentVersionPO> findByEnabledAndAgentId(byte enabled, Integer agentId, Sort sort);
}
