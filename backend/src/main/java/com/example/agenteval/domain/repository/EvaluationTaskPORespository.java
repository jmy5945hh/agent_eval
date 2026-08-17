package com.example.agenteval.domain.repository;

import com.example.agenteval.domain.model.AgentScorePO;
import com.example.agenteval.domain.model.EvaluationTaskPO;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface EvaluationTaskPORespository extends BaseRepository<EvaluationTaskPO, Integer>,
        JpaSpecificationExecutor<EvaluationTaskPO> {

    /**
     * 判断指定 Agent 是否被测评任务引用，用于删除前的依赖检查。
     */
    boolean existsByAgentId(int agentId);

    /**
     * 判断指定 Agent 版本是否被测评任务引用，用于删除前的依赖检查。
     */
    boolean existsByAgentVersionId(int agentVersionId);

    /**
     * 判断指定模型是否被测评任务引用（作为执行模型）。
     */
    boolean existsByModelId(int modelId);

    /**
     * 判断指定模型是否被评分任务引用（作为评分模型）。
     */
    boolean existsByScoringModelId(int scoringModelId);

    /**
     * 判断指定评分标准是否被测评任务引用，用于删除前的依赖检查。
     */
    boolean existsByScoreStandardId(int scoreStandardId);

    /**
     * 根据status查询个数
     *
     * @param status
     * @return
     */
    int countByStatus(Integer status);

    /**
     * 根据status查询
     *
     * @param status
     * @return
     */
    List<EvaluationTaskPO> findByStatus(Integer status);


    /**
     * 查询近30天已完成任务的平均综合分
     *
     * @param status    任务状态（假设 2 表示已完成）
     * @param startTime 30天前的时间点
     * @return 平均分（若无记录则返回 null）
     */
    @Query("SELECT AVG(t.avgScore) FROM EvaluationTaskPO t " +
            "WHERE t.status = :status AND t.scoreTime >= :startTime")
    Double findAvgScoreOfCompletedTasksInLast30Days(@Param("status") int status,
                                                    @Param("startTime") LocalDateTime startTime);


    EvaluationTaskPO findTopFirstByStatusOrderByUpdateTimeDesc(int status);

    @Query("SELECT new com.example.agenteval.domain.model.AgentScorePO(e.agentId, COUNT(e), SUM(e.avgScore)) " +
            "FROM EvaluationTaskPO e GROUP BY e.agentId")
    List<AgentScorePO> findAgentScore();
}
