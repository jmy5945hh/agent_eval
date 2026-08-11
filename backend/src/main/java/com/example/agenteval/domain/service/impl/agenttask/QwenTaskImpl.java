package com.example.agenteval.domain.service.impl.agenttask;

import com.example.agenteval.domain.model.EvaluationCasePO;
import com.example.agenteval.domain.model.pojo.AgentTaskRunReturn;
import com.example.agenteval.domain.model.pojo.TaskBaseInfo;
import com.example.agenteval.domain.service.AgentTaskService;
import com.example.agenteval.domain.service.ExecService;
import com.example.agenteval.domain.service.impl.MinioService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service("qwen")
@Slf4j
public class QwenTaskImpl extends AgentTaskBaseAbstractService implements AgentTaskService {

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    private final ExecService execService;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public QwenTaskImpl(MinioService minioService, ExecService execService) {
        super(minioService);
        this.execService = execService;
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
        //拉取代码,切换分支
        /*String runPathName = cloneAndCheckout(evaluationCasePO.getRepo(), evaluationCasePO.getBranch());*/
        String runPathName = "d:\\temp\\";
        //异步调用
        String sessionId = execService.agentInvoke(runPathName, taskBaseInfo.getAgentInfoPO().getStartCmd(), prompt);
        //返回
        return AgentTaskRunReturn.builder().sessionId(sessionId).repoName(runPathName).build();
    }
}
