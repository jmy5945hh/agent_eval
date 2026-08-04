package com.example.agenteval.domain.repository;

import com.example.agenteval.domain.model.EvaluationTaskPO;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

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
}
