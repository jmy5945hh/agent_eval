package com.example.agenteval.domain.service;

import com.example.agenteval.domain.model.pojo.AgentFinish;
import com.example.agenteval.domain.model.pojo.AgentTaskRunReturn;
import com.example.agenteval.domain.model.pojo.TaskBaseInfo;

public interface AgentTaskService {

    /**
     * 创建任务
     *
     * @param taskBaseInfo
     * @return
     */
    AgentTaskRunReturn createAgentTask(TaskBaseInfo taskBaseInfo);

    /**
     * 任务完成
     *
     * @param agentFinish
     * @return
     */
    Integer caseFinish(AgentFinish agentFinish);

    /**
     * 下一个任务
     *
     * @param taskId
     * @return
     */
    AgentTaskRunReturn runNextCase(Integer taskId);

    /**
     * 开始案例评测
     *
     * @param taskId
     */
    AgentTaskRunReturn evalCase(Integer taskId, boolean firstEval);

    /**
     * 案例评测完成
     *
     * @param agentFinish
     */
    void evalCaseFinish(AgentFinish agentFinish);
}
