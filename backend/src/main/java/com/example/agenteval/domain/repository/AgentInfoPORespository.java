package com.example.agenteval.domain.repository;

import com.example.agenteval.domain.model.AgentInfoPO;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AgentInfoPORespository extends BaseRepository<AgentInfoPO, Integer> {

    /**
     * 根据 Agent 名称查找，用于名称唯一性校验。
     */
    AgentInfoPO findByAgentName(String agentName);

    /**
     * 判断指定名称的 Agent 是否已存在。
     */
    boolean existsByAgentName(String agentName);

    @Query("SELECT m FROM AgentInfoPO m WHERE (:agentName IS NULL OR m.agentName = :agentName)")
    Page<AgentInfoPO> findByAgentName(String agentName, Pageable pageable);

    /**
     * 将除指定ID外的所有 Agent 的 defaultAgent 设置为指定的值
     *
     * @param excludedId   要排除的 ID（保留此 ID 的记录不变）
     * @param defaultValue 要设置的目标值（例如 0 或 1）
     * @return 受影响的行数
     */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE AgentInfoPO a SET a.defaultAgent = :defaultValue WHERE a.id != :excludedId")
    int updateDefaultAgentExceptId(
            @Param("excludedId") Integer excludedId,
            @Param("defaultValue") byte defaultValue
    );


    /**
     * 根据主键id批量查询
     *
     * @param agentIds
     * @return
     */
    List<AgentInfoPO> findByIdIn(List<Integer> agentIds);

    List<AgentInfoPO> findByEnabled(byte enabled, Sort sort);
}
