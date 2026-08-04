package com.example.agenteval.domain.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.ObjUtil;
import com.example.agenteval.application.dto.request.record.RecordListRequest;
import com.example.agenteval.application.dto.response.record.RecordListResponse;
import com.example.agenteval.application.dto.response.record.SummaryDataResponse;
import com.example.agenteval.application.dto.response.task.TaskResponse;
import com.example.agenteval.domain.model.*;
import com.example.agenteval.domain.model.pojo.CaseRun;
import com.example.agenteval.domain.model.pojo.ErrorInfo;
import com.example.agenteval.domain.model.pojo.RunScore;
import com.example.agenteval.domain.repository.*;
import com.example.agenteval.domain.service.RecordQueryService;
import com.example.agenteval.domain.service.specification.EvaluationTaskPOSpecs;
import com.example.agenteval.infrastructure.enums.CaseRunStatusEnum;
import com.example.agenteval.infrastructure.enums.TaskStatusEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 测评记录查询服务实现 — {@link RecordQueryService} 的默认实现。
 *
 * <h4>职责</h4>
 * <ul>
 *   <li>历史记录分页查询：支持 Agent、Model、Status、时间范围多维度筛选。</li>
 *   <li>记录详情聚合：将任务信息、执行统计、评分汇总组装为完整视图。</li>
 * </ul>
 *
 * @see RecordQueryService
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecordQueryServiceImpl implements RecordQueryService {

    private final EvaluationTaskPORespository taskRepository;
    private final TaskCaseRunPORespository caseRunRepository;
    private final TaskCaseScorePORespository caseScoreRepository;
    private final AgentInfoPORespository agentInfoPORespository;
    private final ModelConfigPORespository modelConfigPORespository;

    // ==================== 分页查询 ====================

    /**
     * 分页查询历史测评记录。
     * <p>通过 {@link Specification} 动态构建筛选条件，按创建时间倒序排列。</p>
     */
    @Override
    public Page<TaskResponse> listRecords(int page, int size,
                                          String agentId, String modelId,
                                          String status,
                                          LocalDateTime dateFrom, LocalDateTime dateTo) {
        Specification<EvaluationTaskPO> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (agentId != null && !agentId.isEmpty()) {
                predicates.add(cb.equal(root.get("agentId"), Integer.parseInt(agentId)));
            }
            if (modelId != null && !modelId.isEmpty()) {
                predicates.add(cb.equal(root.get("modelId"), Integer.parseInt(modelId)));
            }
            if (status != null && !status.isEmpty()) {
                predicates.add(cb.equal(root.get("status"), mapTaskStatus(status)));
            }
            if (dateFrom != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createTime"), dateFrom));
            }
            if (dateTo != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createTime"), dateTo));
            }

            return predicates.isEmpty() ? null : cb.and(predicates.toArray(new Predicate[0]));
        };

        // 按创建时间倒序
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<EvaluationTaskPO> result = taskRepository.findAll(spec, pageRequest);

        List<TaskResponse> list = result.getContent().stream()
                .map(TaskResponse::from)
                .collect(Collectors.toList());

        log.debug("Listed records: page={}, size={}, total={}", page, size, result.getTotalElements());
        return new PageImpl<>(list, pageRequest, result.getTotalElements());
    }

    // ==================== 详情查询 ====================

    /**
     * 查询测评记录完整详情。
     * <p>聚合任务基本信息、所有执行记录（含状态/耗时/Token/错误信息/评分），
     * 一次性返回完整视图。</p>
     */
    @Override
    public TaskResponse getRecordDetail(Long taskId) {
        EvaluationTaskPO task = taskRepository.findById(taskId.intValue())
                .orElseThrow(() -> new IllegalArgumentException("任务不存在: " + taskId));

        // 查询所有执行记录
        List<TaskCaseRunPO> runEntities = caseRunRepository.findByTaskId(taskId.intValue());

        // 转换执行记录 → CaseRun（含评分）
        List<CaseRun> runs = runEntities.stream()
                .map(this::toCaseRun)
                .collect(Collectors.toList());

        log.debug("Loaded record detail: taskId={}, runs={}", taskId, runs.size());
        return TaskResponse.from(task).withRuns(runs);
    }

    @Override
    public SummaryDataResponse summaryData() {
        //全部
        long count = taskRepository.count();
        //完成任务
        int finishCount = taskRepository.countByStatus(TaskStatusEnum.RUNNING.getStatus());
        //执行中
        List<EvaluationTaskPO> byStatus = taskRepository.findByStatus(TaskStatusEnum.RUNNING.getStatus());
        //队列中的案例
        List<Integer> taskIds = byStatus.stream().map(EvaluationTaskPO::getId).collect(Collectors.toList());
        List<TaskCaseRunPO> taskCaseRunPOS = caseRunRepository.findByTaskIdIn(taskIds);
        long queueCase = taskCaseRunPOS.stream().filter(item -> CaseRunStatusEnum.QUEUED.getStatus().equals(item.getStatus())).count();
        //近30天平均得分
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        Double avg = taskRepository.findAvgScoreOfCompletedTasksInLast30Days(TaskStatusEnum.COMPLETED.getStatus(), thirtyDaysAgo);
        return SummaryDataResponse.builder().taskCount((int) count).finishCount(finishCount).runCount(CollUtil.isEmpty(byStatus) ? 0 : byStatus.size())
                .queueCases((int) queueCase).averageScore(ObjUtil.isNull(avg) ? 0 : avg.intValue()).build();
    }

    @Override
    public Page<RecordListResponse> recordList(RecordListRequest request) {
        Sort sort = Sort.by(Sort.Direction.DESC, "id");
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);
        Page<EvaluationTaskPO> evaluationTaskList = taskRepository.findAll(EvaluationTaskPOSpecs.recordListBuildSpec(request), pageable);
        //agent
        List<Integer> agentIds = evaluationTaskList.stream().map(EvaluationTaskPO::getAgentId).collect(Collectors.toList());
        List<AgentInfoPO> agentInfoList = agentInfoPORespository.findByIdIn(agentIds);
        Map<Integer, AgentInfoPO> agentInfoMap = agentInfoList.stream().collect(Collectors.toMap(AgentInfoPO::getId, Function.identity()));
        //model
        List<Integer> modelIds = evaluationTaskList.stream().map(EvaluationTaskPO::getModelId).collect(Collectors.toList());
        List<ModelConfigPO> modelConfigList = modelConfigPORespository.findByIdIn(modelIds);
        Map<Integer, ModelConfigPO> modelMap = modelConfigList.stream().collect(Collectors.toMap(ModelConfigPO::getId, Function.identity()));
        //案例个数
        List<Integer> taskId = evaluationTaskList.stream().map(EvaluationTaskPO::getId).collect(Collectors.toList());
        List<TaskCaseRunPO> caseRunList = caseRunRepository.findByTaskIdIn(taskId);
        Map<Integer, List<TaskCaseRunPO>> caseRunGroupMap = caseRunList.stream().collect(Collectors.groupingBy(TaskCaseRunPO::getTaskId));
        List<EvaluationTaskPO> content = evaluationTaskList.getContent();
        List<RecordListResponse> returnList = new ArrayList<>();
        content.forEach(item -> {
            AgentInfoPO agentInfoPO = agentInfoMap.get(item.getAgentId());
            ModelConfigPO modelConfigPO = modelMap.get(item.getModelId());
            List<TaskCaseRunPO> taskCaseRunPOS = caseRunGroupMap.get(item.getId());
            returnList.add(RecordListResponse.builder().taskName(item.getTaskName()).agentName(agentInfoPO.getAgentName()).modelName(modelConfigPO.getModelName())
                    .caseCount(taskCaseRunPOS.size()).taskStatus(item.getStatus()).scoreStatus(item.getScoringStatus()).taskCreateUserName(item.getCreateUserName())
                    .taskCreateTaskTime(LocalDateTimeUtil.formatNormal(item.getCreateTime())).build());
        });

        return new PageImpl<>(returnList, evaluationTaskList.getPageable(), evaluationTaskList.getTotalElements());

    }

    // ==================== 映射转换 ====================

    /**
     * 将 {@link TaskCaseRunPO} 转换为 {@link CaseRun}。
     * <p>同时查询关联的评分维度数据组装 {@link RunScore}。</p>
     */
    private CaseRun toCaseRun(TaskCaseRunPO entity) {
        // 查询评分维度
        List<TaskCaseScorePO> scores = caseScoreRepository.findByRunId(entity.getId());

        RunScore runScore = null;
        if (!scores.isEmpty()) {
            Map<String, Integer> dims = new LinkedHashMap<>();
            Map<String, String> comments = new LinkedHashMap<>();
            for (TaskCaseScorePO s : scores) {
                dims.put(s.getDimKey(), s.getScore());
                if (s.getComment() != null) {
                    comments.put(s.getDimKey(), s.getComment());
                }
            }
            runScore = RunScore.builder()
                    .dims(dims)
                    .comments(comments)
                    .build();
        }

        // 错误信息（简化处理：仅在有 errorInfoKey 时构造）
        ErrorInfo error = null;
        if (entity.getErrorInfoKey() != null && !entity.getErrorInfoKey().isEmpty()) {
            error = new ErrorInfo("agent", entity.getErrorInfoKey());
        }

        return CaseRun.builder()
                .caseId(String.valueOf(entity.getCaseId()))
                .status(mapRunStatus(entity.getStatus()))
                .attempts(entity.getAttempts())
                .rounds(entity.getRounds())
                .tokensIn(entity.getTokensIn())
                .tokensOut(entity.getTokensOut())
                .durationMs(entity.getDurationMs())
                .error(error)
                .score(runScore)
                .removed(false)
                .build();
    }

    // ==================== 状态映射 ====================

    /**
     * 将请求中的状态字符串映射为数据库 int 值。
     * <ul>
     *   <li>running → 1</li>
     *   <li>completed → 2</li>
     *   <li>cancelled → 3</li>
     * </ul>
     */
    private int mapTaskStatus(String status) {
        if (status == null) return 1;
        switch (status.toLowerCase()) {
            case "running":
                return 1;
            case "completed":
                return 2;
            case "cancelled":
                return 3;
            default:
                return 1;
        }
    }

    /**
     * 将执行记录状态 int 映射为字符串。
     * <ul>
     *   <li>1 → "queued"</li>
     *   <li>2 → "running"</li>
     *   <li>3 → "success"</li>
     *   <li>4 → "failed"</li>
     *   <li>5 → "cancelled"</li>
     * </ul>
     */
    private String mapRunStatus(int status) {
        switch (status) {
            case 1:
                return "queued";
            case 2:
                return "running";
            case 3:
                return "success";
            case 4:
                return "failed";
            case 5:
                return "cancelled";
            default:
                return "queued";
        }
    }
}
