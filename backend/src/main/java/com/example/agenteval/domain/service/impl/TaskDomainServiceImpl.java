package com.example.agenteval.domain.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.text.StrPool;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.example.agenteval.application.dto.request.cases.CaseListRequest;
import com.example.agenteval.application.dto.response.evalcase.CaseListResponse;
import com.example.agenteval.application.dto.response.task.*;
import com.example.agenteval.domain.model.*;
import com.example.agenteval.domain.model.pojo.AgentTaskRunReturn;
import com.example.agenteval.domain.model.pojo.TaskBaseInfo;
import com.example.agenteval.domain.repository.*;
import com.example.agenteval.domain.service.AgentTaskService;
import com.example.agenteval.domain.service.EvaluationCaseService;
import com.example.agenteval.domain.service.TaskDomainService;
import com.example.agenteval.domain.service.mapstruct.TaskMapper;
import com.example.agenteval.infrastructure.enums.CaseRunStatusEnum;
import com.example.agenteval.infrastructure.enums.ScoringStatusEnum;
import com.example.agenteval.infrastructure.enums.SwitchStatusEnum;
import com.example.agenteval.infrastructure.enums.TaskStatusEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class TaskDomainServiceImpl implements TaskDomainService {

    private final AgentInfoPORespository agentInfoPORespository;
    private final AgentVersionPORespository agentVersionPORespository;
    private final TaskMapper taskMapper;
    private final ModelConfigPORespository modelConfigPORespository;
    private final ScoringStandardPORespository scoringStandardPORespository;
    private final EvaluationCaseService evaluationCaseService;
    private final EvaluationCasePORespository caseRepository;
    private final AgentTaskService agentTaskService;
    private final EvaluationTaskPORespository evaluationTaskPORespository;
    private final TaskCaseRunPORespository taskCaseRunPORespository;

    @Override
    public List<TaskAgentResponse> taskAgentList() {
        Sort sort = Sort.by(Sort.Direction.DESC, "id");
        List<AgentInfoPO> agentInfoPOList = agentInfoPORespository.findByEnabled(SwitchStatusEnum.ENABLE.getByteStatus(), sort);
        List<Integer> agentIds = agentInfoPOList.stream().map(AgentInfoPO::getId).collect(Collectors.toList());
        List<AgentVersionPO> byAgentIds = agentVersionPORespository.findByAgentIdIn(agentIds);
        Map<Integer, Long> agentVersionMap = byAgentIds.stream().collect(Collectors.groupingBy(AgentVersionPO::getAgentId, Collectors.counting()));
        return agentInfoPOList.stream().map(item -> taskMapper.toTaskAgentResponse(item, agentVersionMap)).collect(Collectors.toList());

    }

    @Override
    public List<TaskAgentVersionResponse> taskAgentVersionList(Integer agentId) {
        Sort sort = Sort.by(Sort.Direction.DESC, "id");
        List<AgentVersionPO> agentVersionPOS = agentVersionPORespository.findByEnabledAndAgentId(SwitchStatusEnum.ENABLE.getByteStatus(), agentId, sort);
        return agentVersionPOS.stream().map(taskMapper::toTaskAgentVersionResponse).collect(Collectors.toList());
    }

    @Override
    public List<TaskModelResponse> taskModelList() {
        Sort sort = Sort.by(Sort.Direction.DESC, "id");
        List<ModelConfigPO> enabledOrderByIdDesc = modelConfigPORespository.findByEnabled(SwitchStatusEnum.ENABLE.getByteStatus(), sort);
        return enabledOrderByIdDesc.stream().map(taskMapper::toTaskModelResponse).collect(Collectors.toList());
    }

    @Override
    public List<TaskScoringStandardResponse> taskScoringStandardList() {
        Sort sort = Sort.by(Sort.Direction.DESC, "id");
        List<ScoringStandardPO> standardPORespositoryAll = scoringStandardPORespository.findAll(sort);
        return standardPORespositoryAll.stream().map(taskMapper::toTaskScoringStandardResponse).collect(Collectors.toList());
    }

    @Override
    public Page<TaskCaseResponse> taskCaseList(CaseListRequest caseListRequest) {
        Page<CaseListResponse> caseListResponses = evaluationCaseService.caseList(caseListRequest);
        return caseListResponses.map(taskMapper::toTaskCaseResponse);
    }

    @Override
    public void createTask(CreateTaskRequest request) {
        //校验
        TaskBaseInfo taskBaseInfo = checkTaskRequestAndGet(request.getAgentId(), request.getAgentVersionId(), request.getModelId(), request.getCaseIds(), request.getScoringStandardId());
        //service执行任务
        AgentTaskRunReturn agentTask = agentTaskService.createAgentTask(taskBaseInfo);
        //数据入库
        String taskName = request.getTaskName();
        if (StrUtil.isBlank(taskName)) {
            taskName = taskBaseInfo.getAgentInfoPO().getAgentName() + StrPool.UNDERLINE + taskBaseInfo.getAgentVersionPO().getVersion() + StrPool.UNDERLINE + DateUtil.format(new Date(), DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + StrPool.UNDERLINE + request.getCreateUserName();
        }
        //获取评分模型
        ModelConfigPO modelConfigPO = modelConfigPORespository.findByScoring((byte) 1);
        EvaluationTaskPO evaluationTaskPO = EvaluationTaskPO.builder().taskName(taskName).agentId(request.getAgentId()).agentVersionId(request.getAgentVersionId())
                .modelId(request.getModelId()).createUserName(request.getCreateUserName()).createUserId("00000000").status(TaskStatusEnum.RUNNING.getStatus())
                .scoreStandardId(request.getScoringStandardId()).scoringStatus(ScoringStatusEnum.IDLE.getStatus()).avgScore(new BigDecimal(0))
                .scoringModelId(modelConfigPO.getId()).build();
        evaluationTaskPORespository.save(evaluationTaskPO);
        List<TaskCaseRunPO> taskCaseRunPOS = new ArrayList<>(taskBaseInfo.getEvaluationCasePOS().size());
        for (int i = 0; i < taskBaseInfo.getEvaluationCasePOS().size(); i++) {
            TaskCaseRunPO taskCaseRunPO = TaskCaseRunPO.builder().taskId(evaluationTaskPO.getId()).caseId(taskBaseInfo.getEvaluationCasePOS().get(i).getId())
                    .attempts(0).rounds(0).tokensIn(0).tokensOut(0).durationMs(0).errorInfoKey("").trajectoryKey("").build();
            if (i == 0) {
                taskCaseRunPO.setSessionId(agentTask.getSessionId());
                taskCaseRunPO.setRepoPath(agentTask.getRepoName());
                taskCaseRunPO.setStatus(CaseRunStatusEnum.RUNNING.getStatus());
            } else {
                taskCaseRunPO.setStatus(CaseRunStatusEnum.QUEUED.getStatus());
            }
            taskCaseRunPOS.add(taskCaseRunPO);
        }
        taskCaseRunPORespository.saveAll(taskCaseRunPOS);
    }

    @Override
    public void stopHook(StopHookRequest stopHookRequest) {
        Integer taskId = agentTaskService.caseFinish(stopHookRequest.getSessionId(), stopHookRequest.getCwd());
        AgentTaskRunReturn agentTaskRunReturn = agentTaskService.runNextCase(taskId);
        TaskCaseRunPO taskCaseRunPO = TaskCaseRunPO.builder().status(CaseRunStatusEnum.RUNNING.getStatus()).build();
        taskCaseRunPO.setId(agentTaskRunReturn.getTaskCaseRunId());
        taskCaseRunPORespository.save(taskCaseRunPO);
    }

    private TaskBaseInfo checkTaskRequestAndGet(Integer agentId, Integer agentVersionId, Integer modelId, List<Integer> caseIds, Integer scoringStandardId) {
        //agent
        AgentInfoPO agentInfoPO = agentInfoPORespository.findById(agentId).orElseThrow(() -> new IllegalArgumentException("Agent 不存在: " + agentId));
        //agent version
        AgentVersionPO agentVersionPO = agentVersionPORespository.findById(agentVersionId).orElseThrow(() -> new IllegalArgumentException("Agent 版本不存在: " + agentVersionId));
        if (!agentId.equals(agentVersionPO.getAgentId())) {
            throw new IllegalArgumentException("agent和agent版本不匹配");
        }
        //model
        ModelConfigPO modelConfigPO = modelConfigPORespository.findById(modelId).orElseThrow(() -> new IllegalArgumentException("模型不存在: " + modelId));
        //case
        List<EvaluationCasePO> casePOList = caseRepository.findByIdIn(caseIds);
        List<Integer> dbCaseIds = casePOList.stream().map(EvaluationCasePO::getId).collect(Collectors.toList());
        if (!NumberUtil.equals(dbCaseIds.size(), caseIds.size())) {
            //找到caseIds有，dbCaseIds没有的
            String notInDbCaseIds = caseIds.stream().filter(item -> !dbCaseIds.contains(item)).map(Object::toString).collect(Collectors.joining(StrPool.COMMA));
            //使用stream通过,拼接成字符串
            throw new IllegalArgumentException("案例不存在: " + notInDbCaseIds);
        }
        //scoring standard
        ScoringStandardPO scoringStandardPO = scoringStandardPORespository.findById(scoringStandardId).orElseThrow(() -> new IllegalArgumentException("评测标准不存在: " + scoringStandardId));
        return TaskBaseInfo.builder().agentInfoPO(agentInfoPO).agentVersionPO(agentVersionPO).modelConfigPO(modelConfigPO)
                .evaluationCasePOS(casePOList).scoringStandardPO(scoringStandardPO).build();
    }


}
