package com.example.agenteval.infrastructure.scoring;

import com.example.agenteval.domain.model.EvaluationCasePO;
import com.example.agenteval.domain.model.EvaluationTaskPO;
import com.example.agenteval.domain.model.ScoringStandardPO;
import com.example.agenteval.domain.model.TaskCaseRunPO;
import com.example.agenteval.domain.model.pojo.*;
import com.example.agenteval.domain.repository.EvaluationCasePORespository;
import com.example.agenteval.domain.repository.EvaluationTaskPORespository;
import com.example.agenteval.domain.repository.ScoringStandardPORespository;
import com.example.agenteval.domain.repository.TaskCaseRunPORespository;
import com.example.agenteval.domain.service.TaskDomainService;
import com.example.agenteval.infrastructure.config.ScoringCliConfig;
import com.example.agenteval.infrastructure.storage.CaseContentService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * 评分客户端服务 — 调用外部评分 CLI 对执行结果自动评分。
 *
 * <h4>评分流程</h4>
 * <ol>
 *   <li>加载当前激活的评分标准。</li>
 *   <li>遍历所有 success 状态的执行记录。</li>
 *   <li>为每条记录调用评分 CLI，解析返回的 JSON 得分。</li>
 *   <li>通过 {@link TaskDomainService#updateRun} 回写评分结果。</li>
 *   <li>计算任务平均分并更新任务实体。</li>
 * </ol>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ScoringClientService {

    private final EvaluationTaskPORespository taskRepository;
    private final EvaluationCasePORespository caseRepository;
    private final ScoringStandardPORespository standardRepository;
    private final TaskCaseRunPORespository caseRunRepository;
    private final TaskDomainService taskDomainService;
    private final ScoringCliConfig config;
    private final CaseContentService caseContentService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final int STATUS_CANCELLED = 3;
    private static final int SCORING_IDLE = 1;
    private static final int SCORING_SCORED = 3;

    /**
     * 异步执行评分。
     */
    @Async("taskExecutor")
    public void scoreTask(Long taskId) {
        EvaluationTaskPO task = taskRepository.findById(taskId.intValue())
                .orElseThrow(() -> new IllegalStateException("Task not found: " + taskId));

        // 获取当前激活的评分标准
        List<ScoringStandardPO> currentStandards = standardRepository.findByIsCurrent((byte) 1);
        ScoringStandardPO standard = currentStandards.isEmpty() ? null : currentStandards.get(0);
        if (standard == null) {
            log.error("No active scoring standard found for task {}", taskId);
            task.setScoringStatus(SCORING_IDLE);
            taskRepository.save(task);
            return;
        }

        // 解析维度
        List<ScoringDimension> dimensions = parseDimensions(standard.getDimensions());
        if (dimensions == null) {
            log.error("Failed to parse dimensions for standard {}", standard.getVersion());
            task.setScoringStatus(SCORING_IDLE);
            taskRepository.save(task);
            return;
        }

        log.info("Starting scoring for task {} with standard {}", taskId, standard.getVersion());
        int tid = taskId.intValue();
        List<TaskCaseRunPO> runEntities = caseRunRepository.findByTaskId(tid);

        double totalScore = 0;
        int scoredCount = 0;

        for (TaskCaseRunPO entity : runEntities) {
            if (entity.getStatus() != 3) continue; // 仅评分成功的案例 (RUN_SUCCESS=3)

            CaseRun run = new CaseRun();
            run.setCaseId(String.valueOf(entity.getCaseId()));

            EvaluationCasePO caseItem = caseRepository.findById(entity.getCaseId()).orElse(null);
            if (caseItem == null) continue;

            int maxRetries = config.getMaxRetries();
            for (int attempt = 0; attempt < maxRetries; attempt++) {
                try {
                    RunScore score = scoreSingleRun(run, caseItem, standard, dimensions);
                    run.setScore(score);
                    break;
                } catch (Exception e) {
                    log.error("Failed to score case {}: {}", run.getCaseId(), e.getMessage());
                    if (attempt == maxRetries - 1) {
                        run.setScore(RunScore.builder()
                                .dims(Collections.emptyMap())
                                .comments(Collections.emptyMap())
                                .analysis("评分失败: " + e.getMessage())
                                .build());
                    }
                }
            }

            taskDomainService.updateRun(taskId, run.getCaseId(), run);

            if (run.getScore() != null && run.getScore().getDims() != null
                    && !run.getScore().getDims().isEmpty()) {
                double avg = run.getScore().getDims().values().stream()
                        .mapToInt(Integer::intValue).average().orElse(0);
                totalScore += avg;
                scoredCount++;
            }
        }

        task.setScoringStatus(SCORING_SCORED);
        if (scoredCount > 0) {
            double avgScore = totalScore / scoredCount;
            task.setAvgScore(java.math.BigDecimal.valueOf(Math.round(avgScore * 100.0) / 100.0));
        }
        taskRepository.save(task);
        log.info("Scoring completed for task {}: {} cases scored, avg={}", taskId, scoredCount,
                task.getAvgScore());
    }

    /**
     * 对单条执行记录评分。
     */
    private RunScore scoreSingleRun(CaseRun run, EvaluationCasePO caseItem,
                                     ScoringStandardPO standard, List<ScoringDimension> dimensions)
            throws Exception {
        String prompt = buildScoringPrompt(run, caseItem, standard, dimensions);
        Path workDir = Files.createTempDirectory(Paths.get(config.getWorkDirPrefix()), "score-");
        String command = buildCommand(caseItem, standard, workDir, prompt);
        Process process = executeCommand(command, workDir);
        List<String> outputLines = captureOutput(process);
        int exitCode = process.waitFor();
        if (exitCode != 0) throw new RuntimeException("Scoring CLI exited with code " + exitCode);
        String jsonStr = extractJsonFromOutput(outputLines);
        Map<String, Object> scoreData = objectMapper.readValue(jsonStr,
                new TypeReference<Map<String, Object>>() {});
        return parseScoreResult(scoreData, dimensions, standard.getVersion());
    }

    // ==================== Prompt & Command ====================

    String buildScoringPrompt(CaseRun run, EvaluationCasePO caseItem,
                               ScoringStandardPO standard, List<ScoringDimension> dimensions) {
        StringBuilder sb = new StringBuilder();
        sb.append("## 评分任务\n\n### 评分维度与权重\n");
        for (ScoringDimension dim : dimensions) {
            sb.append(String.format("- **%s**（%s，权重 %d%%）：%s\n",
                    dim.getLabel(), dim.getKey(), dim.getWeight(),
                    dim.getDesc() != null ? dim.getDesc() : ""));
        }
        sb.append("\n### 案例需求\n**案例名称**：").append(caseItem.getCaseName()).append("\n");
        sb.append("**需求描述**：\n").append(caseContentService.loadPrompt(caseItem)).append("\n\n");

        List<CaseFile> standardAnswer = caseContentService.loadStandardAnswer(caseItem);
        if (standardAnswer != null && !standardAnswer.isEmpty()) {
            sb.append("### 标准答案（参考）\n");
            for (CaseFile f : standardAnswer) {
                sb.append(String.format("**文件 %s**:\n```\n%s\n```\n\n",
                        f.getPath(), readMultipartFileContent(f.getFile())));
            }
        }
        sb.append("### 评分要求\n请根据以上维度对 Agent 的执行结果进行评分（0-100 分），"
                + "严格按以下 JSON 格式返回：\n```json\n"
                + "{\"dimensions\":{...},\"comments\":{...},\"analysis\":\"...\"}\n```");
        return sb.toString();
    }

    String buildCommand(EvaluationCasePO caseItem, ScoringStandardPO standard,
                         Path workDir, String prompt) {
        return config.getCommandTemplate()
                .replace("{prompt}", escapeShell(prompt))
                .replace("{caseName}", caseItem.getCaseName() != null ? caseItem.getCaseName() : "")
                .replace("{standardVersion}", standard.getVersion() != null ? standard.getVersion() : "")
                .replace("{scoringModelId}", "")
                .replace("{workDir}", workDir.toString());
    }

    // ==================== 进程管理 ====================

    private Process executeCommand(String command, Path workDir) throws IOException {
        ProcessBuilder pb = new ProcessBuilder();
        pb.directory(workDir.toFile());
        String os = System.getProperty("os.name").toLowerCase();
        pb.command(os.contains("win") ? "cmd.exe" : "bash",
                os.contains("win") ? "/c" : "-c", command);
        pb.redirectErrorStream(true);
        return pb.start();
    }

    private List<String> captureOutput(Process process) throws Exception {
        List<String> lines = new ArrayList<>();
        ExecutorService exec = Executors.newSingleThreadExecutor();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
            Future<?> f = exec.submit(() -> {
                try {
                    String l;
                    while ((l = reader.readLine()) != null) lines.add(l);
                } catch (IOException ignored) {}
            });
            try {
                f.get(config.getTimeoutSeconds(), TimeUnit.SECONDS);
            } catch (TimeoutException e) {
                process.destroyForcibly();
                f.cancel(true);
                throw e;
            }
        }
        exec.shutdownNow();
        return lines;
    }

    // ==================== JSON 解析 ====================

    String extractJsonFromOutput(List<String> lines) {
        String content = String.join("\n", lines).trim();
        int braceStart = content.indexOf('{');
        int braceEnd = content.lastIndexOf('}');
        if (braceStart >= 0 && braceEnd > braceStart) {
            return content.substring(braceStart, braceEnd + 1).trim();
        }
        return content;
    }

    @SuppressWarnings("unchecked")
    RunScore parseScoreResult(Map<String, Object> response, List<ScoringDimension> dimensions,
                               String standardVersion) {
        Map<String, Object> dimsMap = (Map<String, Object>) response.get("dimensions");
        Map<String, Object> commentsMap = (Map<String, Object>) response.get("comments");
        String analysis = (String) response.get("analysis");

        Map<String, Integer> dims = new LinkedHashMap<>();
        Map<String, String> comments = new LinkedHashMap<>();
        for (ScoringDimension dim : dimensions) {
            Object v = dimsMap != null ? dimsMap.get(dim.getKey()) : null;
            dims.put(dim.getKey(), v instanceof Number ? ((Number) v).intValue() : 0);
            Object c = commentsMap != null ? commentsMap.get(dim.getKey()) : null;
            comments.put(dim.getKey(), c != null ? c.toString() : "");
        }
        return RunScore.builder()
                .dims(dims)
                .comments(comments)
                .analysis(analysis != null ? analysis : "")
                .note("")
                .edited(false)
                .model("scoring-cli")
                .standardVersion(standardVersion)
                .build();
    }

    // ==================== 辅助方法 ====================

    private String escapeShell(String s) {
        if (s == null) return "";
        return s.replace("\"", "\\\"").replace("\n", "\\n");
    }

    /**
     * 将评分标准中的 JSON 字符串解析为维度列表。
     */
    private List<ScoringDimension> parseDimensions(String dimensionsJson) {
        if (dimensionsJson == null || dimensionsJson.isEmpty()) return null;
        try {
            return objectMapper.readValue(dimensionsJson,
                    new TypeReference<List<ScoringDimension>>() {});
        } catch (Exception e) {
            log.error("Failed to parse scoring dimensions", e);
            return null;
        }
    }

    private String readMultipartFileContent(org.springframework.web.multipart.MultipartFile file) {
        if (file == null || file.isEmpty()) return "";
        try {
            return new String(file.getBytes(), java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.warn("Failed to read multipart file content", e);
            return "";
        }
    }
}
