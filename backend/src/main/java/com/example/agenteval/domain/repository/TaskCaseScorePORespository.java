package com.example.agenteval.domain.repository;

import com.example.agenteval.domain.model.TaskCaseScorePO;

import java.util.List;

public interface TaskCaseScorePORespository extends BaseRepository<TaskCaseScorePO, Integer> {

    /**
     * 根据执行记录 ID 查询所有维度评分。
     */
    List<TaskCaseScorePO> findByRunId(int runId);
}
