package com.example.agenteval.domain.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.text.StrPool;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.example.agenteval.application.dto.request.cases.CaseListRequest;
import com.example.agenteval.application.dto.response.evalcase.CaseListResponse;
import com.example.agenteval.application.dto.response.task.*;
import com.example.agenteval.domain.model.*;
import com.example.agenteval.domain.model.pojo.AgentFinish;
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
import org.springframework.scheduling.annotation.Async;
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
    private final TaskCaseEvalLinkPORespository taskCaseEvalLinkPORespository;

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
        log.info("开始创建任务, agentId:{}, agentVersionId:{}, modelId:{}, scoringStandardId:{}, caseIds:{}", request.getAgentId(), request.getAgentVersionId(), request.getModelId(), request.getScoringStandardId(), request.getCaseIds());
        //校验
        TaskBaseInfo taskBaseInfo = checkTaskRequestAndGet(request.getAgentId(), request.getAgentVersionId(), request.getModelId(), request.getCaseIds(), request.getScoringStandardId());
        log.info("任务参数校验通过, agentId:{}, 案例数量:{}", request.getAgentId(), taskBaseInfo.getEvaluationCasePOS().size());
        Thread thread = new Thread(() -> {

            //service执行任务
            AgentTaskRunReturn agentTask = agentTaskService.createAgentTask(taskBaseInfo);
            log.info("agent任务已创建, sessionId:{}", agentTask.getSessionId());
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
                        .attempts(0).rounds(0).tokensIn(0).tokensOut(0).durationMs(0).errorInfoKey("").trajectoryKey("")
                        .evalStatus(CaseRunStatusEnum.QUEUED.getStatus()).build();
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
            log.info("任务数据入库完成, taskId:{}, taskName:{}, 案例数量:{}", evaluationTaskPO.getId(), taskName, taskCaseRunPOS.size());
        });
        thread.start();
    }

    @Override
    @Async
    public void stopHook(StopHookRequest stopHookRequest) {
        log.info("开始处理stop事件hook, sessionId:{}", stopHookRequest.getSessionId());
        Thread thread = new Thread(() -> {
            TaskCaseRunPO bySessionId = taskCaseRunPORespository.findBySessionId(stopHookRequest.getSessionId());
            if (ObjUtil.isNotNull(bySessionId) && CaseRunStatusEnum.RUNNING.getStatus().equals(bySessionId.getStatus())) {
                //原本是运行中，现在运行完了
                log.info("案例执行完成, sessionId:{}, taskId:{}", stopHookRequest.getSessionId(), bySessionId.getTaskId());
                Integer taskId = agentTaskService.caseFinish(AgentFinish.builder().sessionId(stopHookRequest.getSessionId()).cwd(stopHookRequest.getCwd())
                        .error(stopHookRequest.getError()).errorDetails(stopHookRequest.getErrorDetails()).lastAssistantMessage(stopHookRequest.getLastAssistantMessage()).build());
                log.info("案例结果入库完成, taskId:{}", taskId);
                AgentTaskRunReturn agentTaskRunReturn = agentTaskService.runNextCase(taskId);
                log.info("下一个案例已启动, taskId:{}, sessionId:{}", taskId, agentTaskRunReturn.getSessionId());
                return;
            }
            TaskCaseEvalLinkPO byEvalSessionId = taskCaseEvalLinkPORespository.findByEvalSessionId(stopHookRequest.getSessionId());
            if (ObjUtil.isNotNull(byEvalSessionId)) {
                //评测结束，进行下一个评测
                log.info("案例评测完成, evalSessionId:{}, runSessionId:{}", byEvalSessionId.getEvalSessionId(), byEvalSessionId.getRunSessionId());
                agentTaskService.evalCaseFinish(AgentFinish.builder().sessionId(byEvalSessionId.getRunSessionId()).evalSessionId(byEvalSessionId.getEvalSessionId()).cwd(stopHookRequest.getCwd())
                        .error(stopHookRequest.getError()).errorDetails(stopHookRequest.getErrorDetails()).lastAssistantMessage(stopHookRequest.getLastAssistantMessage()).build());
                return;
            }
            log.warn("未找到匹配的运行中案例或评测记录, sessionId:{}", stopHookRequest.getSessionId());
        });
        thread.start();

    }


    private TaskBaseInfo checkTaskRequestAndGet(Integer agentId, Integer agentVersionId, Integer modelId, List<Integer> caseIds, Integer scoringStandardId) {
        log.info("开始校验任务参数, agentId:{}, agentVersionId:{}, modelId:{}, caseIds:{}, scoringStandardId:{}", agentId, agentVersionId, modelId, caseIds, scoringStandardId);
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
            log.error("任务参数校验失败, 案例不存在: {}", notInDbCaseIds);
            throw new IllegalArgumentException("案例不存在: " + notInDbCaseIds);
        }
        //scoring standard
        ScoringStandardPO scoringStandardPO = scoringStandardPORespository.findById(scoringStandardId).orElseThrow(() -> new IllegalArgumentException("评测标准不存在: " + scoringStandardId));
        log.info("任务参数校验完成, agentName:{}, 案例数量:{}", agentInfoPO.getAgentName(), casePOList.size());
        return TaskBaseInfo.builder().agentInfoPO(agentInfoPO).agentVersionPO(agentVersionPO).modelConfigPO(modelConfigPO)
                .evaluationCasePOS(casePOList).scoringStandardPO(scoringStandardPO).build();
    }


}
