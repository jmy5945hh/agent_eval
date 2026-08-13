package com.example.agenteval.domain.repository;

import com.example.agenteval.domain.model.TaskCaseEvalLinkPO;

public interface TaskCaseEvalLinkPORespository extends BaseRepository<TaskCaseEvalLinkPO, Integer> {

    TaskCaseEvalLinkPO findByEvalSessionId(String evalSessionId);
}
