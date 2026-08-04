package com.example.agenteval.domain.service;

import com.example.agenteval.application.dto.ScoreRequest;
import com.example.agenteval.application.dto.request.task.TaskCreateRequest;
import com.example.agenteval.application.dto.response.task.TaskResponse;
import com.example.agenteval.domain.model.EvaluationTaskPO;
import com.example.agenteval.domain.model.TaskCaseRunPO;
import com.example.agenteval.domain.model.pojo.CaseRun;
import com.example.agenteval.domain.model.pojo.ErrorInfo;
import com.example.agenteval.domain.repository.EvaluationTaskPORespository;
import com.example.agenteval.domain.repository.TaskCaseRunPORespository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 任务领域服务 — 负责测评任务的全生命周期管理。
 *
 * <h4>职责</h4>
 * <ul>
 *   <li>任务 CRUD：创建、查询列表、查询详情。</li>
 *   <li>执行控制：终止案例、添加案例、移除案例、重跑案例、取消任务。</li>
 *   <li>评分触发：校验任务状态后异步发起评分。</li>
 *   <li>执行记录管理：初始化 runs、更新 run 状态、检测任务完成。</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TaskDomainService {

    private final EvaluationTaskPORespository taskRepository;
    private final TaskCaseRunPORespository caseRunRepository;

    // ==================== 任务查询 ====================

    /**
     * 获取所有测评任务列表，按创建时间倒序。
     */
    public List<TaskResponse> getAllTasks() {
        return taskRepository.findAll(Sort.by(Sort.Direction.DESC, "createTime")).stream()
                .map(task -> TaskResponse.from(task).withRuns(new ArrayList<>()))
                .collect(Collectors.toList());
    }

    /**
     * 根据 ID 查询任务详情（含 runs 列表）。
     */
    public Optional<TaskResponse> getTaskById(Long taskId) {
        return taskRepository.findById(taskId.intValue())
                .map(task -> {
                    List<CaseRun> runs = getRunsByTaskId(taskId);
                    return TaskResponse.from(task).withRuns(runs);
                });
    }

    /**
     * 获取任务下所有案例的执行记录列表。
     */
    public List<CaseRun> getRunsByTaskId(Long taskId) {
        List<TaskCaseRunPO> entities = caseRunRepository.findByTaskId(taskId.intValue());
        return entities.stream()
                .map(this::toCaseRun)
                .collect(Collectors.toList());
    }

    // ==================== 任务创建 ====================

    /**
     * 创建测评任务并异步启动执行。
     * <p>校验参数后创建 Task 和 TaskCaseRun，调用 AgentExecutorService 串行执行。</p>
     */
    @Transactional
    public TaskResponse createTask(TaskCreateRequest request) {
        String taskName = (request.getTaskName() != null && !request.getTaskName().isBlank())
                ? request.getTaskName()
                : "task-" + System.currentTimeMillis();

        EvaluationTaskPO task = EvaluationTaskPO.builder()
                .taskName(taskName)
                .agentId(parseInt(request.getAgentId()))
                .agentVersionId(parseIntOrZero(request.getAgentVersionId()))
                .modelId(parseInt(request.getModelId()))
                .scoreStandardId(parseIntOrZero(request.getScoringStandardId()))
                .createUserName("demo-user")
                .status(STATUS_RUNNING)
                .scoringStatus(SCORING_IDLE)
                .build();

        EvaluationTaskPO saved = taskRepository.save(task);
        int taskId = saved.getId();

        List<CaseRun> runs = initRuns(taskId, request.getSelectedCases());
        log.info("Task created: id={}, name={} with {} cases", taskId, taskName, runs.size());

        return TaskResponse.from(saved).withRuns(runs);
    }

    // ==================== 评分触发 ====================

    /**
     * 发起自动评分。
     * <p>任务状态必须为 completed 且 scoringStatus 不能已是 scoring。</p>
     */
    @Transactional
    public TaskResponse scoreTask(Long taskId, ScoreRequest request) {
        EvaluationTaskPO task = taskRepository.findById(taskId.intValue())
                .orElseThrow(() -> new IllegalArgumentException("任务不存在: " + taskId));

        if (task.getStatus() != STATUS_COMPLETED) {
            throw new IllegalStateException("任务未完成，无法评分");
        }
        if (task.getScoringStatus() == SCORING_IN_PROGRESS) {
            throw new IllegalStateException("评分已在进行中");
        }

        task.setScoringModelId(parseInt(request.getScoringModelId()));
        task.setScoringStatus(SCORING_IN_PROGRESS);
        taskRepository.save(task);

        log.info("Scoring prepared for task {}", taskId);

        return getTaskById(taskId).orElse(null);
    }

    // ==================== 执行控制 ====================

    /**
     * 终止指定案例的执行。
     * <p>running 的案例终止进程，queued 的直接标记 cancelled。</p>
     */
    @Transactional
    public TaskResponse terminateRun(Long taskId, String caseId) {
        CaseRun cancelledRun = CaseRun.builder()
                .caseId(caseId)
                .status("cancelled")
                .error(new ErrorInfo("手动终止", "Terminated by user"))
                .build();

        updateRun(taskId, caseId, cancelledRun);
        log.info("Terminated run: task={}, case={}", taskId, caseId);

        return getTaskById(taskId).orElse(null);
    }

    // ==================== 执行记录管理 ====================

    /**
     * 初始化任务执行记录（TaskCaseRun）。
     */
    @Transactional
    public List<CaseRun> initRuns(int taskId, List<String> caseIds) {
        List<CaseRun> runs = new ArrayList<>();
        for (String cid : caseIds) {
            TaskCaseRunPO entity = TaskCaseRunPO.builder()
                    .taskId(taskId)
                    .caseId(parseInt(cid))
                    .status(RUN_QUEUED)
                    .attempts(0)
                    .rounds(0)
                    .tokensIn(0)
                    .tokensOut(0)
                    .durationMs(0L)
                    .build();
            caseRunRepository.save(entity);
            runs.add(CaseRun.builder()
                    .caseId(cid)
                    .status("queued")
                    .attempts(0)
                    .build());
        }
        return runs;
    }

    /**
     * 更新执行记录状态。
     */
    @Transactional
    public void updateRun(Long taskId, String caseId, CaseRun updatedRun) {
        int tid = taskId.intValue();
        int cid = parseInt(caseId);

        // 查找已有记录或新建
        List<TaskCaseRunPO> existing = caseRunRepository.findByTaskId(tid);
        TaskCaseRunPO entity = existing.stream()
                .filter(r -> r.getCaseId() == cid)
                .findFirst()
                .orElse(TaskCaseRunPO.builder()
                        .taskId(tid)
                        .caseId(cid)
                        .build());

        entity.setStatus(mapRunStatusToInt(updatedRun.getStatus()));
        if (updatedRun.getAttempts() != null) entity.setAttempts(updatedRun.getAttempts());
        if (updatedRun.getRounds() != null) entity.setRounds(updatedRun.getRounds());
        if (updatedRun.getTokensIn() != null) entity.setTokensIn(updatedRun.getTokensIn());
        if (updatedRun.getTokensOut() != null) entity.setTokensOut(updatedRun.getTokensOut());
        if (updatedRun.getDurationMs() != null) entity.setDurationMs(updatedRun.getDurationMs());
        if (updatedRun.getError() != null) {
            entity.setErrorInfoKey(updatedRun.getError().getCategory() + ":" + updatedRun.getError().getLog());
        }
        caseRunRepository.save(entity);

        // 检查任务是否全部完成
        checkTaskCompletion(tid);
    }

    // ==================== 动态管理案例 ====================

    /**
     * 向已有任务动态添加案例。
     * <p>仅 running 状态可添加；已存在的 caseId 自动跳过。</p>
     */
    @Transactional
    public List<CaseRun> addCasesToTask(Long taskId, List<String> caseIds) {
        EvaluationTaskPO task = getTaskEntity(taskId);
        if (task.getStatus() != STATUS_RUNNING) {
            throw new IllegalStateException("仅运行中的任务可添加案例");
        }

        int tid = taskId.intValue();
        List<Integer> existingCaseIds = caseRunRepository.findByTaskId(tid).stream()
                .map(TaskCaseRunPO::getCaseId)
                .collect(Collectors.toList());

        List<CaseRun> newRuns = new ArrayList<>();
        for (String cid : caseIds) {
            int caseId = parseInt(cid);
            if (existingCaseIds.contains(caseId)) {
                log.info("Case {} already in task {}, skipped", caseId, taskId);
                continue;
            }
            TaskCaseRunPO entity = TaskCaseRunPO.builder()
                    .taskId(tid)
                    .caseId(caseId)
                    .status(RUN_QUEUED)
                    .attempts(0)
                    .rounds(0)
                    .tokensIn(0)
                    .tokensOut(0)
                    .durationMs(0L)
                    .build();
            caseRunRepository.save(entity);
            newRuns.add(CaseRun.builder().caseId(cid).status("queued").attempts(0).build());
        }
        log.info("Added {} cases to task {}", newRuns.size(), taskId);
        return newRuns;
    }

    /**
     * 从任务中移除指定案例执行记录。
     */
    @Transactional
    public void removeRun(Long taskId, Long runId, String reason) {
        TaskCaseRunPO run = caseRunRepository.findById(runId.intValue())
                .orElseThrow(() -> new IllegalArgumentException("执行记录不存在: " + runId));
        if (run.getStatus() == RUN_RUNNING || run.getStatus() == RUN_QUEUED) {
            run.setStatus(RUN_CANCELLED);
        }
        caseRunRepository.save(run);
        log.info("Removed run {} from task {}, reason={}", runId, taskId, reason);
    }

    /**
     * 重跑单条案例，重置状态为 queued。
     */
    @Transactional
    public void rerunCase(Long taskId, Long runId) {
        TaskCaseRunPO run = caseRunRepository.findById(runId.intValue())
                .orElseThrow(() -> new IllegalArgumentException("执行记录不存在: " + runId));
        run.setStatus(RUN_QUEUED);
        run.setRounds(0);
        run.setTokensIn(0);
        run.setTokensOut(0);
        run.setDurationMs(0L);
        run.setErrorInfoKey(null);
        run.setAttempts(run.getAttempts() + 1);
        caseRunRepository.save(run);
        log.info("Rerun case: task={}, run={}, attempts={}", taskId, runId, run.getAttempts());
    }

    /**
     * 取消整个任务，终止所有未完成案例。
     */
    @Transactional
    public void cancelTask(Long taskId) {
        EvaluationTaskPO task = getTaskEntity(taskId);
        if (task.getStatus() != STATUS_RUNNING) {
            throw new IllegalStateException("仅运行中的任务可取消");
        }
        task.setStatus(STATUS_CANCELLED);
        taskRepository.save(task);

        int tid = taskId.intValue();
        List<TaskCaseRunPO> runs = caseRunRepository.findByTaskId(tid);
        for (TaskCaseRunPO run : runs) {
            if (run.getStatus() == RUN_RUNNING || run.getStatus() == RUN_QUEUED) {
                run.setStatus(RUN_CANCELLED);
                caseRunRepository.save(run);
            }
        }
        log.info("Cancelled task {}: {} runs terminated", taskId, runs.size());
    }

    // ==================== 内部辅助方法 ====================

    private EvaluationTaskPO getTaskEntity(Long taskId) {
        return taskRepository.findById(taskId.intValue())
                .orElseThrow(() -> new IllegalArgumentException("任务不存在: " + taskId));
    }

    /**
     * 检查任务下所有 run 是否都已完成，是则标记任务为 completed。
     */
    private void checkTaskCompletion(int taskId) {
        List<TaskCaseRunPO> allRuns = caseRunRepository.findByTaskId(taskId);
        boolean allDone = allRuns.stream()
                .allMatch(r -> r.getStatus() == RUN_SUCCESS
                        || r.getStatus() == RUN_FAILED
                        || r.getStatus() == RUN_CANCELLED);
        if (allDone) {
            taskRepository.findById(taskId).ifPresent(task -> {
                if (task.getStatus() != STATUS_CANCELLED) {
                    task.setStatus(STATUS_COMPLETED);
                    taskRepository.save(task);
                    log.info("Task {} all runs complete", taskId);
                }
            });
        }
    }

    /**
     * TaskCaseRunPO → CaseRun 转换。
     */
    private CaseRun toCaseRun(TaskCaseRunPO entity) {
        ErrorInfo error = null;
        if (entity.getErrorInfoKey() != null && !entity.getErrorInfoKey().isEmpty()) {
            String[] parts = entity.getErrorInfoKey().split(":", 2);
            error = new ErrorInfo(parts[0], parts.length > 1 ? parts[1] : "");
        }
        return CaseRun.builder()
                .caseId(String.valueOf(entity.getCaseId()))
                .status(mapRunStatusToString(entity.getStatus()))
                .attempts(entity.getAttempts())
                .rounds(entity.getRounds())
                .tokensIn(entity.getTokensIn())
                .tokensOut(entity.getTokensOut())
                .durationMs(entity.getDurationMs())
                .error(error)
                .removed(false)
                .build();
    }

    // ==================== 状态常量 ====================

    // 任务状态
    private static final int STATUS_RUNNING = 1;
    private static final int STATUS_COMPLETED = 2;
    private static final int STATUS_CANCELLED = 3;

    // 评分状态
    private static final int SCORING_IDLE = 1;
    private static final int SCORING_IN_PROGRESS = 2;

    // 执行记录状态
    private static final int RUN_QUEUED = 1;
    private static final int RUN_RUNNING = 2;
    private static final int RUN_SUCCESS = 3;
    private static final int RUN_FAILED = 4;
    private static final int RUN_CANCELLED = 5;

    // ==================== 类型转换与映射 ====================

    private int parseInt(String s) {
        return Integer.parseInt(s);
    }

    private int parseIntOrZero(String s) {
        if (s == null || s.isEmpty()) return 0;
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private int mapRunStatusToInt(String status) {
        if (status == null) return RUN_QUEUED;
        switch (status.toLowerCase()) {
            case "queued":   return RUN_QUEUED;
            case "running":  return RUN_RUNNING;
            case "success":  return RUN_SUCCESS;
            case "failed":   return RUN_FAILED;
            case "cancelled": return RUN_CANCELLED;
            default:         return RUN_QUEUED;
        }
    }

    private String mapRunStatusToString(int status) {
        switch (status) {
            case RUN_QUEUED:   return "queued";
            case RUN_RUNNING:  return "running";
            case RUN_SUCCESS:  return "success";
            case RUN_FAILED:   return "failed";
            case RUN_CANCELLED: return "cancelled";
            default:           return "queued";
        }
    }
}
