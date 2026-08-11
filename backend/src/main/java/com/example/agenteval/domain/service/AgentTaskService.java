package com.example.agenteval.domain.service;

import com.example.agenteval.domain.model.pojo.AgentTaskRunReturn;
import com.example.agenteval.domain.model.pojo.TaskBaseInfo;

public interface AgentTaskService {

    AgentTaskRunReturn createAgentTask(TaskBaseInfo taskBaseInfo);

    Integer caseFinish(String sessionId, String cwd);

    AgentTaskRunReturn runNextCase(Integer taskId);

}
