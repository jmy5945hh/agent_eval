package com.example.agenteval.domain.service.impl.agenttask;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ObjUtil;
import com.example.agenteval.domain.model.AgentInfoPO;
import com.example.agenteval.domain.model.EvaluationCasePO;
import com.example.agenteval.domain.model.EvaluationTaskPO;
import com.example.agenteval.domain.model.TaskCaseRunPO;
import com.example.agenteval.domain.model.pojo.AgentTaskRunReturn;
import com.example.agenteval.domain.model.pojo.TaskBaseInfo;
import com.example.agenteval.domain.model.pojo.agent.QwenCaseRun;
import com.example.agenteval.domain.model.pojo.agent.QwenJsonL;
import com.example.agenteval.domain.repository.*;
import com.example.agenteval.domain.service.AgentTaskService;
import com.example.agenteval.domain.service.ExecService;
import com.example.agenteval.domain.service.impl.MinioService;
import com.example.agenteval.infrastructure.enums.CaseRunStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Service("qwen")
@Slf4j
public class QwenTaskImpl extends AgentTaskBaseAbstractService implements AgentTaskService {

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    private final ExecService execService;
    private final TaskCaseRunPORespository taskCaseRunPORespository;
    private final TaskCaseScorePORespository taskCaseScorePORespository;
    private final AgentInfoPORespository agentInfoPORespository;
    private final EvaluationTaskPORespository evaluationTaskPORespository;
    private final EvaluationCasePORespository evaluationCasePORespository;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public QwenTaskImpl(MinioService minioService, ExecService execService, TaskCaseRunPORespository taskCaseRunPORespository, TaskCaseScorePORespository taskCaseScorePORespository, AgentInfoPORespository agentInfoPORespository, EvaluationTaskPORespository evaluationTaskPORespository, EvaluationCasePORespository evaluationCasePORespository) {
        super(minioService);
        this.execService = execService;
        this.taskCaseRunPORespository = taskCaseRunPORespository;
        this.taskCaseScorePORespository = taskCaseScorePORespository;
        this.agentInfoPORespository = agentInfoPORespository;
        this.evaluationTaskPORespository = evaluationTaskPORespository;
        this.evaluationCasePORespository = evaluationCasePORespository;
    }

    @Override
    public AgentTaskRunReturn createAgentTask(TaskBaseInfo taskBaseInfo) {
        //读取配置文件
        String localConfig = readSettings(taskBaseInfo.getAgentInfoPO().getConfigPath());
        //合并配置文件
        String newConfig = mergeConfig(localConfig, taskBaseInfo.getAgentVersionPO().getContentOsPath(), taskBaseInfo.getModelConfigPO().getAuthorization(),
                taskBaseInfo.getModelConfigPO().getModelName(), taskBaseInfo.getModelConfigPO().getEndpoint());
        //写入到本地文件中
        writeConfigFile(taskBaseInfo.getAgentInfoPO().getConfigPath(), newConfig);
        //prompt读取
        EvaluationCasePO evaluationCasePO = taskBaseInfo.getEvaluationCasePOS().get(0);
        String prompt = getPrompt(evaluationCasePO.getPromptKey());
        String sessionId = UUID.randomUUID().toString();
        FileUtil.mkdir(evaluationCasePO.getRepo() + File.separator + sessionId);
        //拉取代码,切换分支
        /*String runPathName = cloneAndCheckout(evaluationCasePO.getRepo(), evaluationCasePO.getBranch(), sessionId);*/
        String runPathName = "D:\\temp\\ant-design-pro-for-edd";
        //异步调用
        execService.agentInvoke(runPathName, taskBaseInfo.getAgentInfoPO().getStartCmd(), prompt, sessionId);
        //返回
        return AgentTaskRunReturn.builder().sessionId(sessionId).repoName(runPathName).build();
    }

    @Override
    public Integer caseFinish(String sessionId, String cwd) {
        TaskCaseRunPO taskCaseRunPO = taskCaseRunPORespository.findBySessionId(sessionId);
        Path logDir = Paths.get(System.getProperty("user.home"), ".qwen", "eval-logs");
        File outputLog = logDir.resolve("qwen_" + sessionId + "_out.log").toFile();
        File errorLog = logDir.resolve("qwen_" + sessionId + "_err.log").toFile();
        //读取正确输出
        uploadAgentFileToOOS(outputLog);
        //读取错误输出
        boolean errorFlag = uploadAgentFileToOOS(errorLog);
        //读取jsonL
        String projectFolder = cwd.replace(":", "").replaceAll("\\\\", "-").replaceAll("/", "-");
        Path sessionDir = Paths.get(System.getProperty("user.home"), ".qwen", "projects", projectFolder, "chats");
        File sessionJsonL = sessionDir.resolve(sessionId + ".jsonl").toFile();
        List<QwenJsonL> qwenJsonL = readAndUploadAgentFileToOOS(sessionJsonL, QwenJsonL.class);
        //统计
        QwenCaseRun qwenCaseRun = summaryStatistics(qwenJsonL);
        //入库
        TaskCaseRunPO updateTaskCaseRun = TaskCaseRunPO.builder().status(CaseRunStatusEnum.SUCCESS.getStatus()).attempts(0).rounds(1)
                .tokensIn(qwenCaseRun.getTokenIn()).tokensOut(qwenCaseRun.getTokenOut()).durationMs(qwenCaseRun.getDurationMs())
                .errorInfoKey(errorFlag ? errorLog.getName() : "").trajectoryKey(sessionJsonL.getName()).build();
        updateTaskCaseRun.setId(taskCaseRunPO.getId());
        taskCaseRunPORespository.save(updateTaskCaseRun);
        return taskCaseRunPO.getTaskId();
    }

    @Override
    public AgentTaskRunReturn runNextCase(Integer taskId) {
        //任务
        EvaluationTaskPO evaluationTaskPO = evaluationTaskPORespository.findById(taskId).orElseThrow(() -> new IllegalArgumentException("任务不存在: " + taskId));
        //任务案例关联表
        TaskCaseRunPO taskCaseRunPO = taskCaseRunPORespository.finByNextRunCase(taskId);
        if (ObjUtil.isNull(taskCaseRunPO)) {
            //TODO 最后一个,进行测评
        }
        //案例
        EvaluationCasePO evaluationCasePO = evaluationCasePORespository.findById(taskCaseRunPO.getCaseId()).orElseThrow(() -> new IllegalArgumentException("案例不存在: " + taskCaseRunPO.getCaseId()));
        AgentInfoPO agentInfoPO = agentInfoPORespository.findById(evaluationTaskPO.getAgentId()).orElseThrow(() -> new IllegalArgumentException("Agent 不存在: " + evaluationTaskPO.getAgentId()));

        String prompt = getPrompt(evaluationCasePO.getPromptKey());
        String sessionId = UUID.randomUUID().toString();
        FileUtil.mkdir(evaluationCasePO.getRepo() + File.separator + sessionId);
        //拉取代码,切换分支
        /*String runPathName = cloneAndCheckout(evaluationCasePO.getRepo(), evaluationCasePO.getBranch(), sessionId);*/
        String runPathName = "D:\\temp\\ant-design-pro-for-edd";
        //异步调用
        execService.agentInvoke(runPathName, agentInfoPO.getStartCmd(), prompt, sessionId);
        //返回
        return AgentTaskRunReturn.builder().sessionId(sessionId).repoName(runPathName).taskCaseRunId(taskCaseRunPO.getId()).build();
    }

    /**
     * 处理统计数据
     *
     * @param qwenJsonL
     */
    private QwenCaseRun summaryStatistics(List<QwenJsonL> qwenJsonL) {
        int tokenOut = qwenJsonL.stream()
                .map(QwenJsonL::getUsageMetadata)
                .filter(Objects::nonNull)
                .mapToInt(usage -> Optional.ofNullable(usage.getPromptTokenCount()).orElse(0))
                .sum();
        int tokenIn = qwenJsonL.stream()
                .map(QwenJsonL::getUsageMetadata)
                .filter(Objects::nonNull)
                .mapToInt(usage -> Optional.ofNullable(usage.getCandidatesTokenCount()).orElse(0))
                .sum();
        String startTime = qwenJsonL.get(0).getTimestamp();
        String endTime = qwenJsonL.get(qwenJsonL.size() - 1).getTimestamp();
        Date startDate = DateUtil.parse(startTime, DatePattern.UTC_MS_WITH_ZONE_OFFSET_PATTERN);
        Date endDate = DateUtil.parse(endTime, DatePattern.UTC_MS_WITH_ZONE_OFFSET_PATTERN);
        long durationMs = DateUtil.betweenMs(startDate, endDate);
        return QwenCaseRun.builder().tokenIn(tokenIn).tokenOut(tokenOut).durationMs(durationMs).build();
    }
}
