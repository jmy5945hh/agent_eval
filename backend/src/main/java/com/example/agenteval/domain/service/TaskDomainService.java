package com.example.agenteval.domain.service;

import com.example.agenteval.application.dto.request.cases.CaseListRequest;
import com.example.agenteval.application.dto.response.task.*;
import org.springframework.data.domain.Page;

import javax.validation.Valid;
import java.util.List;

public interface TaskDomainService {
    /**
     * 获取可用Agent列表
     *
     * @return
     */
    List<TaskAgentResponse> taskAgentList();

    /**
     * 获取可用Agent版本列表
     *
     * @return
     */
    List<TaskAgentVersionResponse> taskAgentVersionList(Integer agentId);

    /**
     * 获取可用模型列表
     *
     * @return
     */
    List<TaskModelResponse> taskModelList();

    /**
     * 获取可用评分标准列表
     *
     * @return
     */
    List<TaskScoringStandardResponse> taskScoringStandardList();

    /**
     * 获取案例列表
     *
     * @return
     */
    Page<TaskCaseResponse> taskCaseList(CaseListRequest caseListRequest);

    /**
     * 创建任务
     *
     * @param request
     */
    void createTask(@Valid CreateTaskRequest request);
}
