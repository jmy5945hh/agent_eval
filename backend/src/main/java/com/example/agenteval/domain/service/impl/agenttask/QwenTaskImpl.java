package com.example.agenteval.domain.service.impl.agenttask;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.TypeReference;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.example.agenteval.domain.model.*;
import com.example.agenteval.domain.model.pojo.*;
import com.example.agenteval.domain.model.pojo.agent.QwenCaseRun;
import com.example.agenteval.domain.model.pojo.agent.QwenJsonL;
import com.example.agenteval.domain.repository.*;
import com.example.agenteval.domain.service.AgentTaskService;
import com.example.agenteval.domain.service.ExecService;
import com.example.agenteval.domain.service.impl.MinioService;
import com.example.agenteval.domain.service.mapstruct.ScoreCommentResultMapper;
import com.example.agenteval.infrastructure.constant.ScoreConstant;
import com.example.agenteval.infrastructure.enums.CaseRunStatusEnum;
import com.example.agenteval.infrastructure.enums.ScoringStatusEnum;
import com.example.agenteval.infrastructure.enums.TaskStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Service("qwen")
@Slf4j
public class QwenTaskImpl extends AgentTaskBaseAbstractService implements AgentTaskService {

    private static final String SUCCESS_TARGET = "\"event.name\":\"qwen-code.api_response\"";
    private static final String FAILURE_TARGET = "\"event.name\":\"qwen-code.api_error\"";
    private final AgentVersionPORespository agentVersionPORespository;
    private final ModelConfigPORespository modelConfigPORespository;
    private final ScoringStandardPORespository scoringStandardPORespository;
    private final TaskCaseEvalLinkPORespository taskCaseEvalLinkPORespository;
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    private final ExecService execService;
    private final TaskCaseRunPORespository taskCaseRunPORespository;
    private final TaskCaseScorePORespository taskCaseScorePORespository;
    private final AgentInfoPORespository agentInfoPORespository;
    private final EvaluationTaskPORespository evaluationTaskPORespository;
    private final EvaluationCasePORespository evaluationCasePORespository;
    private final ScoreCommentResultMapper scoreCommentResultMapper;
    private final CaseAnswerListPORespository caseAnswerListPORespository;
    @Value("${eval-case-prompt}")
    private String evalCasePrompt;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public QwenTaskImpl(MinioService minioService, ExecService execService, TaskCaseRunPORespository taskCaseRunPORespository, TaskCaseScorePORespository taskCaseScorePORespository, AgentInfoPORespository agentInfoPORespository, EvaluationTaskPORespository evaluationTaskPORespository, EvaluationCasePORespository evaluationCasePORespository, AgentVersionPORespository agentVersionPORespository, ModelConfigPORespository modelConfigPORespository, ScoringStandardPORespository scoringStandardPORespository, ScoringStandardPORespository scoringStandardPORespository1, ScoreCommentResultMapper scoreCommentResultMapper, CaseAnswerListPORespository caseAnswerListPORespository, TaskCaseEvalLinkPORespository taskCaseEvalLinkPORespository, TaskCaseEvalLinkPORespository taskCaseEvalLinkPORespository1) {
        super(minioService);
        this.execService = execService;
        this.taskCaseRunPORespository = taskCaseRunPORespository;
        this.taskCaseScorePORespository = taskCaseScorePORespository;
        this.agentInfoPORespository = agentInfoPORespository;
        this.evaluationTaskPORespository = evaluationTaskPORespository;
        this.evaluationCasePORespository = evaluationCasePORespository;
        this.agentVersionPORespository = agentVersionPORespository;
        this.modelConfigPORespository = modelConfigPORespository;
        this.scoringStandardPORespository = scoringStandardPORespository1;
        this.scoreCommentResultMapper = scoreCommentResultMapper;
        this.caseAnswerListPORespository = caseAnswerListPORespository;
        this.taskCaseEvalLinkPORespository = taskCaseEvalLinkPORespository1;
    }

    @Override
    public AgentTaskRunReturn createAgentTask(TaskBaseInfo taskBaseInfo) {
        log.info("开始创建agent任务, agentName:{}, caseId:{}", taskBaseInfo.getAgentInfoPO().getAgentName(), taskBaseInfo.getEvaluationCasePOS().get(0).getId());
        //读取配置文件
        String localConfig = readSettings(taskBaseInfo.getAgentInfoPO().getConfigPath());
        //合并配置文件
        String newConfig = mergeConfig(localConfig, taskBaseInfo.getAgentVersionPO().getContentOsPath(), taskBaseInfo.getModelConfigPO().getAuthorization(), taskBaseInfo.getModelConfigPO().getModelName(), taskBaseInfo.getModelConfigPO().getEndpoint());
        //写入到本地文件中
        writeConfigFile(taskBaseInfo.getAgentInfoPO().getConfigPath(), newConfig);
        log.info("agent配置已合并写入, configPath:{}", taskBaseInfo.getAgentInfoPO().getConfigPath());
        //prompt读取
        EvaluationCasePO evaluationCasePO = taskBaseInfo.getEvaluationCasePOS().get(0);
        String prompt = getPrompt(evaluationCasePO.getPromptKey());
        String sessionId = UUID.randomUUID().toString();
        FileUtil.mkdir(evaluationCasePO.getRepo() + File.separator + sessionId);
        //拉取代码,切换分支
        String runPathName = cloneAndCheckout(evaluationCasePO.getRepo(), evaluationCasePO.getBranch(), sessionId);
        /*String runPathName = "D:\\temp\\ant-design-pro-for-edd";*/
        //异步调用
        execService.agentInvoke(runPathName, taskBaseInfo.getAgentInfoPO().getStartCmd(), prompt, sessionId);
        //返回
        log.info("agent任务创建完成, sessionId:{}, 运行目录:{}", sessionId, runPathName);
        return AgentTaskRunReturn.builder().sessionId(sessionId).repoName(runPathName).build();
    }

    @Override
    public Integer caseFinish(AgentFinish agentFinish) {
        log.info("开始处理案例完成, sessionId:{}", agentFinish.getSessionId());
        TaskCaseRunPO taskCaseRunPO = taskCaseRunPORespository.findBySessionId(agentFinish.getSessionId());
        Path logDir = Paths.get(System.getProperty("user.home"), ".qwen", "eval-logs");
        File errorLog = logDir.resolve("qwen_" + agentFinish.getSessionId() + "_err.log").toFile();
        boolean errorFlag = StrUtil.isNotBlank(agentFinish.getError());
        String fileName = "";
        if (errorFlag) {
            log.warn("案例执行存在错误, sessionId:{}, error:{}", agentFinish.getSessionId(), agentFinish.getError());
            AgentFinish tempError = AgentFinish.builder().error(agentFinish.getError()).errorDetails(agentFinish.getErrorDetails()).lastAssistantMessage(agentFinish.getLastAssistantMessage()).build();
            fileName = "qwen_" + agentFinish.getSessionId() + "_error.log";
            minioService.createAndUploadFile(fileName, JSONUtil.toJsonStr(tempError));
        }
        //读取正确输出
        /*uploadAgentFileToOOS(outputLog);*/
        minioService.createAndUploadFile("qwen_" + agentFinish.getSessionId() + "_out.log", agentFinish.getLastAssistantMessage());
        //读取错误输出
        uploadAgentFileToOOS(errorLog);
        //读取jsonL
        String projectFolder = agentFinish.getCwd().replace(":\\", "--").replaceAll("\\\\", "-").replaceAll("/", "-");
        /*Path sessionDir = Paths.get(System.getProperty("user.home"), ".qwen", "projects", projectFolder, "chats");
        File sessionJsonL = sessionDir.resolve(agentFinish.getSessionId() + ".jsonl").toFile();*/
        File sessionJsonL = new File(agentFinish.getTranscriptPath());
        List<QwenJsonL> qwenJsonL = readAndUploadAgentJsonLFileToOOS(sessionJsonL, QwenJsonL.class);
        //统计
        QwenCaseRun qwenCaseRun = summaryStatistics(qwenJsonL);
        //入库
        taskCaseRunPO.setStatus(!errorFlag ? CaseRunStatusEnum.SUCCESS.getStatus() : CaseRunStatusEnum.FAILED.getStatus());
        taskCaseRunPO.setTokensIn(qwenCaseRun.getTokenIn());
        taskCaseRunPO.setTokensOut(qwenCaseRun.getTokenOut());
        taskCaseRunPO.setDurationMs(qwenCaseRun.getDurationMs());
        taskCaseRunPO.setTrajectoryKey(sessionJsonL.getName());
        taskCaseRunPO.setRounds(qwenCaseRun.getTurn());
        taskCaseRunPORespository.save(taskCaseRunPO);
        errorLog.delete();
        sessionJsonL.delete();
        log.info("案例完成处理结束, sessionId:{}, taskId:{}, 状态:{}", agentFinish.getSessionId(), taskCaseRunPO.getTaskId(), taskCaseRunPO.getStatus());
        return taskCaseRunPO.getTaskId();
    }

    @Override
    public AgentTaskRunReturn runNextCase(Integer taskId) {
        //任务
        EvaluationTaskPO evaluationTaskPO = evaluationTaskPORespository.findById(taskId).orElseThrow(() -> new IllegalArgumentException("任务不存在: " + taskId));
        //任务案例关联表
        TaskCaseRunPO taskCaseRunPO = taskCaseRunPORespository.findFirstByTaskIdAndStatusOrderByCreateTimeDesc(taskId, CaseRunStatusEnum.QUEUED.getStatus());
        if (ObjUtil.isNull(taskCaseRunPO)) {
            log.info("无排队中的案例, 进入评测阶段, taskId:{}", taskId);
            evaluationTaskPO.setStatus(TaskStatusEnum.COMPLETED.getStatus());
            evaluationTaskPORespository.save(evaluationTaskPO);
            return evalCase(taskId, true);
        }
        log.info("开始执行下一个案例, taskId:{}", taskCaseRunPO.getId());
        //案例
        EvaluationCasePO evaluationCasePO = evaluationCasePORespository.findById(taskCaseRunPO.getCaseId()).orElseThrow(() -> new IllegalArgumentException("案例不存在: " + taskCaseRunPO.getCaseId()));
        AgentInfoPO agentInfoPO = agentInfoPORespository.findById(evaluationTaskPO.getAgentId()).orElseThrow(() -> new IllegalArgumentException("Agent 不存在: " + evaluationTaskPO.getAgentId()));

        String prompt = getPrompt(evaluationCasePO.getPromptKey());
        String sessionId = UUID.randomUUID().toString();
        FileUtil.mkdir(evaluationCasePO.getRepo() + File.separator + sessionId);
        //拉取代码,切换分支
        String runPathName = cloneAndCheckout(evaluationCasePO.getRepo(), evaluationCasePO.getBranch(), sessionId);
        /*String runPathName = "D:\\temp\\ant-design-pro-for-edd";*/
        //异步调用
        execService.agentInvoke(runPathName, agentInfoPO.getStartCmd(), prompt, sessionId);
        taskCaseRunPO.setSessionId(sessionId);
        taskCaseRunPO.setRepoPath(runPathName);
        taskCaseRunPO.setStatus(CaseRunStatusEnum.RUNNING.getStatus());
        taskCaseRunPORespository.save(taskCaseRunPO);
        //返回
        log.info("下一个案例已启动, runId:{}, caseId:{}, sessionId:{}", taskCaseRunPO.getId(), taskCaseRunPO.getCaseId(), sessionId);
        return AgentTaskRunReturn.builder().sessionId(sessionId).repoName(runPathName).taskCaseRunId(taskCaseRunPO.getId()).build();
    }

    @Override
    public AgentTaskRunReturn evalCase(Integer taskId, boolean firstEval) {
        log.info("开始案例评测, taskId:{}, firstEval:{}", taskId, firstEval);
        EvaluationTaskPO evaluationTaskPO = evaluationTaskPORespository.findById(taskId).orElseThrow(() -> new IllegalArgumentException("任务不存在: " + taskId));
        //先找到任务中第一条评测的案例
        TaskCaseRunPO evalTaskCaseRun = taskCaseRunPORespository.findFirstByTaskIdAndStatusAndEvalStatusOrderByCreateTimeDesc(taskId, CaseRunStatusEnum.SUCCESS.getStatus(), CaseRunStatusEnum.QUEUED.getStatus());
        if (ObjUtil.isNull(evalTaskCaseRun)) {
            //针对只有一个案例，跑完后，立马停止评测
            finishEval(taskId);
            return AgentTaskRunReturn.builder().build();
        }
        //agent
        AgentInfoPO agentInfoPO = agentInfoPORespository.findById(evaluationTaskPO.getAgentId()).orElseThrow(() -> new IllegalArgumentException("Agent不存在: " + evaluationTaskPO.getAgentId()));
        if (firstEval && !ObjUtil.equals(evaluationTaskPO.getModelId(), evaluationTaskPO.getScoringModelId())) {
            //agent version
            AgentVersionPO agentVersionPO = agentVersionPORespository.findById(evaluationTaskPO.getAgentVersionId()).orElseThrow(() -> new IllegalArgumentException("Agent版本不存在: " + evaluationTaskPO.getAgentVersionId()));
            //模型
            ModelConfigPO modelConfigPO = modelConfigPORespository.findById(evaluationTaskPO.getScoringModelId()).orElseThrow(() -> new IllegalArgumentException("模型: " + evaluationTaskPO.getAgentVersionId()));
            //读取配置文件
            String localConfig = readSettings(agentInfoPO.getConfigPath());
            //合并配置文件
            String newConfig = mergeConfig(localConfig, agentVersionPO.getContentOsPath(), modelConfigPO.getAuthorization(), modelConfigPO.getModelName(), modelConfigPO.getEndpoint());
            //写入到本地文件中
            writeConfigFile(agentInfoPO.getConfigPath(), newConfig);
            log.info("首次评测已切换评分模型配置, configPath:{}", agentInfoPO.getConfigPath());
        }

        //评分标准
        ScoringStandardPO scoringStandardPO = scoringStandardPORespository.findById(evaluationTaskPO.getScoreStandardId()).orElseThrow(() -> new IllegalArgumentException("评分标准不存在: " + taskId));
        String dimensions = scoringStandardPO.getDimensions();
        List<ScoringDimension> scoringDimensions = JSONUtil.toBean(dimensions, new TypeReference<>() {
        }, true);
        List<ScoreCommentResult> dimensionsResultList = scoringDimensions.stream().map(scoreCommentResultMapper::toDimensionsResult).collect(Collectors.toList());
        String dimensionsResult = JSONUtil.toJsonStr(dimensionsResultList);
        //标准答案
        List<CaseAnswerListPO> caseAnswerListPOS = caseAnswerListPORespository.findByCaseId(evalTaskCaseRun.getCaseId());
        StringBuffer stringBuffer = new StringBuffer();
        caseAnswerListPOS.forEach(item -> {
            stringBuffer.append(ScoreConstant.FILE_PATH);
            stringBuffer.append(item.getFilePath());
            stringBuffer.append("\n");
            String content = minioService.getAndReadFile(item.getStandardAnswerKey());
            stringBuffer.append(ScoreConstant.FILE_CONTENT);
            stringBuffer.append(content);
            stringBuffer.append("\n\n");
        });

        EvaluationCasePO evaluationCasePO = evaluationCasePORespository.findById(evalTaskCaseRun.getCaseId()).orElseThrow(() -> new IllegalArgumentException("案例不存在: " + evalTaskCaseRun.getCaseId()));
        String taskPrompt = minioService.getAndReadFile(evaluationCasePO.getPromptKey());
        String resultPrompt = evalCasePrompt.replace(ScoreConstant.TASK_DESCRIPTION, taskPrompt).replace(ScoreConstant.CORRECT_REFERENCE_DOCUMENT, stringBuffer.toString()).replace(ScoreConstant.SCORING_CRITERIA, dimensions).replace(ScoreConstant.OUTPUT_FORMAT, dimensionsResult);
        String sessionId = UUID.randomUUID().toString();
        execService.agentInvoke(evalTaskCaseRun.getRepoPath(), agentInfoPO.getStartCmd(), resultPrompt, sessionId);
        taskCaseEvalLinkPORespository.save(TaskCaseEvalLinkPO.builder().runSessionId(evalTaskCaseRun.getSessionId()).evalSessionId(sessionId).build());
        evalTaskCaseRun.setEvalStatus(CaseRunStatusEnum.RUNNING.getStatus());
        evalTaskCaseRun.setStatus(CaseRunStatusEnum.SUCCESS.getStatus());
        taskCaseRunPORespository.save(evalTaskCaseRun);
        if (firstEval) {
            evaluationTaskPO.setScoringStatus(ScoringStatusEnum.SCORING.getStatus());
            evaluationTaskPORespository.save(evaluationTaskPO);
        }

        log.info("案例评测已启动, taskId:{}, caseId:{}, evalSessionId:{}", taskId, evalTaskCaseRun.getCaseId(), sessionId);
        return AgentTaskRunReturn.builder().sessionId(sessionId).taskCaseRunId(evalTaskCaseRun.getId()).build();

    }

    private void finishEval(Integer taskId) {
        //评测完成
        log.info("所有案例评测完成, 开始汇总评分, taskId:{}", taskId);
        EvaluationTaskPO evaluationTaskPO = evaluationTaskPORespository.findById(taskId).orElseThrow(() -> new IllegalArgumentException("任务不存在: " + taskId));
        evaluationTaskPO.setScoringStatus(ScoringStatusEnum.SCORED.getStatus());
        //求平均分
        List<TaskCaseRunPO> byTaskId = taskCaseRunPORespository.findByTaskIdAndStatusNot(evaluationTaskPO.getId(), CaseRunStatusEnum.CANCELLED.getStatus());
        BigDecimal averageScore;
        if (byTaskId.isEmpty()) {
            // 可根据业务逻辑设定默认值，例如 0 或 null
            averageScore = BigDecimal.ZERO;
        } else {
            int totalScore = byTaskId.stream().mapToInt(item -> item.getScore().intValue()).sum();
            averageScore = BigDecimal.valueOf(totalScore).divide(BigDecimal.valueOf(byTaskId.size()), 2, RoundingMode.HALF_UP);
        }
        evaluationTaskPO.setAvgScore(averageScore);
        evaluationTaskPORespository.save(evaluationTaskPO);
        log.info("任务评测完成, taskId:{}, 平均分:{}", evaluationTaskPO.getId(), averageScore);
    }

    @Override
    public void evalCaseFinish(AgentFinish agentFinish) {
        log.info("开始处理案例评测完成, evalSessionId:{}, runSessionId:{}", agentFinish.getEvalSessionId(), agentFinish.getSessionId());
        TaskCaseRunPO taskCaseRunPO = taskCaseRunPORespository.findBySessionId(agentFinish.getSessionId());
        Path logDir = Paths.get(System.getProperty("user.home"), ".qwen", "eval-logs");
        File errorLog = logDir.resolve("qwen_" + agentFinish.getEvalSessionId() + "_err.log").toFile();
        boolean errorFlag = StrUtil.isNotBlank(agentFinish.getError());
        String fileName = "";
        if (errorFlag) {
            log.warn("评测执行存在错误, evalSessionId:{}, error:{}", agentFinish.getEvalSessionId(), agentFinish.getError());
            AgentFinish tempError = AgentFinish.builder().error(agentFinish.getError()).errorDetails(agentFinish.getErrorDetails()).lastAssistantMessage(agentFinish.getLastAssistantMessage()).build();
            fileName = "qwen_" + agentFinish.getEvalSessionId() + "_error.log";
            minioService.createAndUploadFile(fileName, JSONUtil.toJsonStr(tempError));
        }
        //读取正确输出
        String output = agentFinish.getLastAssistantMessage();
        //读取错误输出
        uploadAgentFileToOOS(errorLog);
        //上传jsonL
        String projectFolder = agentFinish.getCwd().replace(":\\", "--").replaceAll("\\\\", "-").replaceAll("/", "-");
        Path sessionDir = Paths.get(System.getProperty("user.home"), ".qwen", "projects", projectFolder, "chats");
        File sessionJsonL = sessionDir.resolve(agentFinish.getEvalSessionId() + ".jsonl").toFile();
        uploadAgentFileToOOS(sessionJsonL);
        //读取评分结果
        List<ScoreCommentResult> dimensionsResultList = new ArrayList<>();
        try {
            dimensionsResultList = JSONUtil.toBean(output, new TypeReference<>() {
            }, true);
        } catch (Exception e) {
            String regex = "\\[\\s*\\{[\\s\\S]*?\\}(?:\\s*,\\s*\\{[\\s\\S]*?\\})*\\s*\\]";
            String score = ReUtil.get(regex, output, 0);
            score = score.replaceAll("\\n", "");
            if (StrUtil.isNotBlank(score)) {
                try {
                    dimensionsResultList = JSONUtil.toBean(score, new TypeReference<>() {
                    }, true);
                } catch (Exception ex) {
                    log.error("读取评测{}结果异常{}", agentFinish.getSessionId(), e.getMessage(), e);
                }
            } else {
                log.error("读取评测{}结果异常{}", agentFinish.getSessionId(), e.getMessage(), e);
            }
        }

        if (CollUtil.isNotEmpty(dimensionsResultList)) {
            List<TaskCaseScorePO> taskCaseScorePOS = new ArrayList<>(dimensionsResultList.size());
            dimensionsResultList.forEach(item -> {
                taskCaseScorePOS.add(TaskCaseScorePO.builder().runId(taskCaseRunPO.getId()).dimKey(item.getKey()).dimLabel(item.getLabel()).score(item.getScore()).comment(item.getComment()).build());
                taskCaseScorePORespository.saveAll(taskCaseScorePOS);
            });
        }
        int sum = dimensionsResultList.stream().mapToInt(ScoreCommentResult::getScore).sum();
        taskCaseRunPO.setScore(new BigDecimal(sum));
        taskCaseRunPO.setEvalStatus(CaseRunStatusEnum.SUCCESS.getStatus());
        taskCaseRunPORespository.save(taskCaseRunPO);
        log.info("案例评测结果入库完成, runSessionId:{}, 评分维度数量:{}", agentFinish.getSessionId(), CollUtil.size(dimensionsResultList));
        //继续下一个评测
        TaskCaseRunPO nextTaskCaseRun = taskCaseRunPORespository.findFirstByTaskIdAndStatusAndEvalStatusOrderByCreateTimeDesc(taskCaseRunPO.getTaskId(), CaseRunStatusEnum.SUCCESS.getStatus(), CaseRunStatusEnum.QUEUED.getStatus());
        if (ObjUtil.isNull(nextTaskCaseRun)) {
            //评测完成
            finishEval(taskCaseRunPO.getTaskId());
            return;
        }
        evalCase(taskCaseRunPO.getTaskId(), false);
    }

    /**
     * 处理统计数据
     *
     * @param qwenJsonL
     */
    private QwenCaseRun summaryStatistics(List<QwenJsonL> qwenJsonL) {
        log.info("开始统计案例执行数据, 记录条数:{}", qwenJsonL.size());
        int tokenOut = qwenJsonL.stream().map(QwenJsonL::getUsageMetadata).filter(Objects::nonNull).mapToInt(usage -> Optional.ofNullable(usage.getPromptTokenCount()).orElse(0)).sum();
        int tokenIn = qwenJsonL.stream().map(QwenJsonL::getUsageMetadata).filter(Objects::nonNull).mapToInt(usage -> Optional.ofNullable(usage.getCandidatesTokenCount()).orElse(0)).sum();
        String startTime = qwenJsonL.get(0).getTimestamp();
        String endTime = qwenJsonL.get(qwenJsonL.size() - 1).getTimestamp();
        Date startDate = DateUtil.parse(startTime, DatePattern.UTC_MS_FORMAT);
        Date endDate = DateUtil.parse(endTime, DatePattern.UTC_MS_FORMAT);
        long durationMs = DateUtil.betweenMs(startDate, endDate);
        String jsonStr = JSONUtil.toJsonStr(qwenJsonL);
        Integer successTurn = getTurn(SUCCESS_TARGET, jsonStr);
        Integer failTurn = getTurn(FAILURE_TARGET, jsonStr);

        log.info("案例执行统计完成, tokenIn:{}, tokenOut:{}, durationMs:{}", tokenIn, tokenOut, durationMs);
        return QwenCaseRun.builder().tokenIn(tokenIn).tokenOut(tokenOut).durationMs(durationMs).turn(successTurn + failTurn).build();
    }

    private Integer getTurn(String target, String jsonLContent) {

        int count = 0;
        int fromIndex = 0;

        while (true) {
            int index = jsonLContent.indexOf(target, fromIndex);
            if (index == -1) {
                break;
            }
            count++;
            fromIndex = index + target.length(); // 跳过本次匹配，继续向后查找
        }

        return count;
    }
}
