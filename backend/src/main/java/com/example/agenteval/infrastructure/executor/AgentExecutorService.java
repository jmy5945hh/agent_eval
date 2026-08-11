package com.example.agenteval.infrastructure.executor;

import com.example.agenteval.domain.model.EvaluationCasePO;
import com.example.agenteval.domain.model.EvaluationTaskPO;
import com.example.agenteval.domain.model.TaskCaseRunPO;
import com.example.agenteval.domain.model.pojo.CaseRun;
import com.example.agenteval.domain.model.pojo.ErrorInfo;
import com.example.agenteval.domain.model.pojo.TrajectoryEntry;
import com.example.agenteval.domain.repository.EvaluationCasePORespository;
import com.example.agenteval.domain.repository.EvaluationTaskPORespository;
import com.example.agenteval.domain.repository.TaskCaseRunPORespository;
import com.example.agenteval.domain.service.TaskDomainService;
import com.example.agenteval.infrastructure.config.AgentCliConfig;
import com.example.agenteval.infrastructure.storage.CaseContentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Agent 执行引擎 — 负责在独立线程中串行执行测评任务的各案例。
 *
 * <h4>执行流程</h4>
 * <ol>
 *   <li>从数据库加载任务及其关联的所有执行记录。</li>
 *   <li>按顺序遍历每条执行记录，调用 CLI 执行 Agent。</li>
 *   <li>每条记录最多重试 {@link AgentCliConfig#getMaxRetries()} 次。</li>
 *   <li>实时采集执行轨迹（stdout 解析为 TrajectoryEntry 列表）。</li>
 *   <li>执行完毕后通过 {@link TaskDomainService#} 回写状态。</li>
 * </ol>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AgentExecutorService {

    // 执行记录状态常量（与 TaskDomainService 保持一致）
    private static final int RUN_QUEUED = 1;
    private static final int RUN_RUNNING = 2;
    private static final int RUN_SUCCESS = 3;
    private static final int RUN_FAILED = 4;
    private static final int RUN_CANCELLED = 5;
    // 任务状态常量
    private static final int STATUS_RUNNING = 1;
    private static final int STATUS_COMPLETED = 2;
    private static final int STATUS_CANCELLED = 3;
    private final EvaluationTaskPORespository taskRepository;
    private final EvaluationCasePORespository caseRepository;
    private final TaskCaseRunPORespository caseRunRepository;
    private final TaskDomainService taskDomainService;
    private final AgentCliConfig config;
    private final CaseContentService caseContentService;
    private final Map<String, Process> runningProcesses = new ConcurrentHashMap<>();
    private final Map<Long, Boolean> cancelledTasks = new ConcurrentHashMap<>();

    /**
     * 异步执行测评任务。
     * <p>遍历所有关联的执行记录，逐条调用 CLI 执行，完成后更新任务状态。</p>
     */
    @Async("taskExecutor")
    public void executeTask(Long taskId) {
        EvaluationTaskPO task = taskRepository.findById(taskId.intValue())
                .orElseThrow(() -> new IllegalStateException("Task not found: " + taskId));

        int tid = taskId.intValue();
        List<TaskCaseRunPO> runs = caseRunRepository.findByTaskId(tid);
        log.info("Starting task execution: {} with {} cases", taskId, runs.size());

        for (TaskCaseRunPO runEntity : runs) {
            if (Boolean.TRUE.equals(cancelledTasks.get(taskId))) {
                log.info("Task {} was cancelled", taskId);
                break;
            }

            EvaluationCasePO caseItem = caseRepository.findById(runEntity.getCaseId())
                    .orElse(null);
            if (caseItem == null) {
                log.warn("Case not found: {}", runEntity.getCaseId());
                continue;
            }

            executeWithRetry(taskId, runEntity, caseItem);
        }

        // 若未被取消，标记任务完成
        if (!Boolean.TRUE.equals(cancelledTasks.get(taskId))) {
            taskRepository.findById(tid).ifPresent(t -> {
                if (t.getStatus() != STATUS_CANCELLED) {
                    t.setStatus(STATUS_COMPLETED);
                    taskRepository.save(t);
                }
            });
        }
        cancelledTasks.remove(taskId);
    }

    /**
     * 带重试的单案例执行。
     */
    private void executeWithRetry(Long taskId, TaskCaseRunPO runEntity, EvaluationCasePO caseItem) {
        int maxRetries = config.getMaxRetries();
        String caseIdStr = String.valueOf(runEntity.getCaseId());

        for (int attempt = 0; attempt < maxRetries; attempt++) {
            CaseRun running = CaseRun.builder()
                    .caseId(caseIdStr)
                    .status("running")
                    .attempts(attempt + 1)
                    .build();
            //taskDomainService.updateRun(taskId, caseIdStr, running);

            long startTime = System.currentTimeMillis();
            try {
                Path workDir = prepareWorkDir(caseIdStr, caseItem);
                String command = buildCommand(caseItem, workDir);
                Process process = executeCommand(command, workDir);
                List<TrajectoryEntry> trajectory = captureOutput(process, command);
                int exitCode = process.waitFor();
                long duration = System.currentTimeMillis() - startTime;

                CaseRun result = CaseRun.builder()
                        .caseId(caseIdStr)
                        .durationMs(duration)
                        .trajectory(trajectory)
                        .status(evaluateExitCode(exitCode))
                        .build();

                if (exitCode != 0) {
                    result.setError(new ErrorInfo("Agent执行失败", "exit code " + exitCode));
                }

                //taskDomainService.updateRun(taskId, caseIdStr, result);
                return;
            } catch (Exception e) {
                log.error("Case {} attempt {} error: {}", runEntity.getCaseId(), attempt + 1, e.getMessage());
                if (attempt == maxRetries - 1) {
                    CaseRun failed = CaseRun.builder()
                            .caseId(caseIdStr)
                            .status("failed")
                            .error(new ErrorInfo("执行异常", e.getMessage()))
                            .build();
                    //taskDomainService.updateRun(taskId, caseIdStr, failed);
                }
            }
        }
    }

    // ==================== 工作目录 & 命令构建 ====================

    /**
     * 为案例执行准备临时工作目录。
     */
    private Path prepareWorkDir(String caseId, EvaluationCasePO caseItem) throws IOException {
        Path baseDir = Paths.get(config.getWorkDirPrefix());
        Files.createDirectories(baseDir);
        return Files.createTempDirectory(baseDir, "case-");
    }

    /**
     * 根据案例信息和配置模板构建 CLI 命令。
     */
    String buildCommand(EvaluationCasePO caseItem, Path workDir) {
        String prompt = caseContentService.loadPrompt(caseItem);
        String repo = caseItem.getRepo() != null ? caseItem.getRepo() : "";
        String branch = caseItem.getBranch() != null ? caseItem.getBranch() : "main";
        return config.getCommandTemplate()
                .replace("{prompt}", escapeShell(prompt))
                .replace("{repo}", repo)
                .replace("{branch}", branch)
                .replace("{workDir}", workDir.toString())
                .replace("{caseName}", caseItem.getCaseName() != null ? caseItem.getCaseName() : "");
    }

    // ==================== 进程管理 ====================

    /**
     * 启动物理进程执行 CLI 命令。
     */
    private Process executeCommand(String command, Path workDir) throws IOException {
        ProcessBuilder pb = new ProcessBuilder();
        pb.directory(workDir.toFile());
        String os = System.getProperty("os.name").toLowerCase();
        pb.command(os.contains("win") ? "cmd.exe" : "bash",
                os.contains("win") ? "/c" : "-c", command);
        pb.redirectErrorStream(true);
        return pb.start();
    }

    /**
     * 捕获进程 stdout 并逐行解析为轨迹条目。
     */
    private List<TrajectoryEntry> captureOutput(Process process, String command) throws Exception {
        List<TrajectoryEntry> trajectory = new ArrayList<>();
        DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm:ss");
        trajectory.add(new TrajectoryEntry("tool", "cmd",
                LocalTime.now().format(timeFmt), "执行命令", command));

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                trajectory.add(parseLine(line, timeFmt));
            }
        }
        return trajectory;
    }

    /**
     * 将一行 stdout 输出解析为轨迹条目。
     * <p>支持的标记：[USER]、[AGENT]、[THINK]、[TOOL:xxx]。</p>
     */
    TrajectoryEntry parseLine(String line, DateTimeFormatter timeFmt) {
        String time = LocalTime.now().format(timeFmt);
        if (line.startsWith("[USER]")) {
            return new TrajectoryEntry("user", "text", time, null,
                    line.substring(6).trim());
        }
        if (line.startsWith("[AGENT]")) {
            return new TrajectoryEntry("agent", "text", time, null,
                    line.substring(7).trim());
        }
        if (line.startsWith("[THINK]")) {
            return new TrajectoryEntry("agent", "think", time, null,
                    line.substring(7).trim());
        }
        if (line.startsWith("[TOOL:")) {
            int end = line.indexOf("]");
            if (end > 0) {
                String kind = line.substring(6, end).trim();
                String content = line.substring(end + 1).trim();
                return new TrajectoryEntry("tool", kind, time, content, content);
            }
        }
        return new TrajectoryEntry("tool", "raw", time, null, line);
    }

    // ==================== 执行控制 ====================

    /**
     * 终止指定案例的执行进程（简化实现）。
     */
    public void terminateRun(Long taskId, String caseId) {
        String key = taskId + ":" + caseId;
        Process process = runningProcesses.get(key);
        if (process != null && process.isAlive()) {
            process.destroyForcibly();
            runningProcesses.remove(key);
            log.info("Terminated process for task={}, case={}", taskId, caseId);
        }
    }

    /**
     * 将新增案例加入执行队列。
     */
    public void enqueueCases(Long taskId, List<String> caseIds) {
        log.info("Enqueuing {} cases for task {}", caseIds.size(), taskId);
    }

    /**
     * 取消整个任务的执行。
     */
    public void cancelTask(Long taskId) {
        cancelledTasks.put(taskId, true);
    }

    // ==================== 辅助方法 ====================

    /**
     * Shell 特殊字符转义。
     */
    private String escapeShell(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("$", "\\$")
                .replace("`", "\\`")
                .replace("\n", "\\n");
    }

    /**
     * 根据退出码判断执行结果。
     */
    private String evaluateExitCode(int exitCode) {
        return exitCode == 0 ? "success" : "failed";
    }
}
