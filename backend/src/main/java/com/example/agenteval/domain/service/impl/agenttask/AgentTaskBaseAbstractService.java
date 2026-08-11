package com.example.agenteval.domain.service.impl.agenttask;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.text.StrPool;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.example.agenteval.domain.service.impl.MinioService;
import com.example.agenteval.infrastructure.constant.ModelConfigConstant;
import com.example.agenteval.infrastructure.util.AgentJsonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.charset.StandardCharsets;

@Service
@Slf4j
@RequiredArgsConstructor
public abstract class AgentTaskBaseAbstractService {

    protected final MinioService minioService;
    @Value("${task-run-folder}")
    private String taskRunFolder;

    /**
     * 读取agent本地配置
     *
     * @param fileName
     * @return
     */
    protected String readSettings(String fileName) {
        //String filePath = resolveConfigPath(fileName);
        try {
            return FileUtil.readString(fileName, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("读取文件{}失败，失败原因{}", fileName, e.getMessage(), e);
            return "{}";
        }
    }

    /**
     * 写入新配置
     *
     * @param filePath
     * @param newConfig
     */
    protected void writeConfigFile(String filePath, String newConfig) {
        FileUtil.writeUtf8String(newConfig, filePath);
    }

    /**
     * 读取prompt
     *
     * @param promptKey
     * @return
     */
    protected String getPrompt(String promptKey) {
        //prompt读取
        return minioService.getAndReadFile(promptKey);
    }

    /**
     * 克隆仓库
     *
     * @param repositoryName
     */
    protected String cloneAndCheckout(String repositoryName, String branch) {
        String directory = repositoryName.replace("https://", "").replace("http://", "").replace(".git", "").split(StrPool.SLASH)[2];
        String pathName = taskRunFolder + File.separator + directory;
        try (Git git = Git.cloneRepository().setURI(repositoryName).setDirectory(new File(pathName)).call()) {
            log.info("克隆仓库完成:{}", repositoryName);
            git.checkout()
                    .setName(branch)
                    .setStartPoint("origin/" + branch)
                    .setCreateBranch(true) //
                    .call();
        } catch (GitAPIException e) {
            log.error("克隆仓库{}出现异常{}", repositoryName, e.getMessage(), e);
            throw new RuntimeException(e);
        }
        return pathName;
    }


    /**
     * 合并2个配置文件
     *
     * @param sourceContent
     * @param configKey
     * @return
     */
    protected String mergeConfig(String sourceContent, String configKey, String apiKey, String modelName, String
            url) {
        String oosContent = minioService.getAndReadFile(configKey);
        oosContent = oosContent.replaceAll(ModelConfigConstant.API_KEY, apiKey).replaceAll(ModelConfigConstant.MODEL_NAME, modelName)
                .replaceAll(ModelConfigConstant.URL, url);
        JSONObject sourceJsonObject = JSONUtil.parseObj(sourceContent);
        JSONObject oosJsonObject = JSONUtil.parseObj(oosContent);
        JSONObject mergeResult = AgentJsonUtil.merge(sourceJsonObject, oosJsonObject);
        return JSONUtil.toJsonStr(mergeResult);
    }

    /**
     * 解析配置文件路径，将 ./ 开头的相对路径替换为用户主目录
     */
    private String resolveConfigPath(String configFilePath) {
        if (configFilePath == null || configFilePath.trim().isEmpty()) {
            throw new IllegalArgumentException("配置文件路径不能为空");
        }
        String path = configFilePath.trim();

        // 1. 如果以 "./" 开头，替换为用户主目录
        if (path.startsWith("~/")) {
            String relative = path.substring(2); // 去掉 "./"
            // 确保相对路径不以 / 或 \ 开头（避免双斜杠）
            if (relative.startsWith("/") || relative.startsWith("\\")) {
                relative = relative.substring(1);
            }
            String userHome = System.getProperty("user.home");
            return userHome + File.separator + relative;
        }

        return path;
    }
}


//判断配置文件是否存在
//合并配置
//写入配置
//运行任务