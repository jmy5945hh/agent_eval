package com.example.agenteval.domain.service.impl.agenttask;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.text.StrPool;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.example.agenteval.domain.service.impl.MinioService;
import com.example.agenteval.infrastructure.constant.ModelConfigConstant;
import com.example.agenteval.infrastructure.util.AgentJsonUtil;
import com.example.agenteval.infrastructure.util.JsonlUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

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
            String content = FileUtil.readString(fileName, StandardCharsets.UTF_8);
            log.info("读取agent本地配置成功, 文件:{}", fileName);
            return content;
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
        log.info("配置写入本地文件完成, 文件:{}", filePath);
    }

    /**
     * 读取prompt
     *
     * @param promptKey
     * @return
     */
    protected String getPrompt(String promptKey) {
        //prompt读取
        log.info("开始读取prompt, promptKey:{}", promptKey);
        String prompt = minioService.getAndReadFile(promptKey);
        log.info("prompt读取完成, promptKey:{}, 长度:{}", promptKey, prompt.length());
        return prompt;
    }

    /**
     * 克隆仓库
     *
     * @param repositoryName
     */
    protected String cloneAndCheckout(String repositoryName, String branch, String sessionId) {
        String directory = repositoryName.replace("https://", "").replace("http://", "").replace(".git", "").split(StrPool.SLASH)[2];
        String pathName = taskRunFolder + File.separator + sessionId + File.separator + directory;
        log.info("开始克隆仓库, repositoryName:{}, branch:{}, sessionId:{}, 目标目录:{}", repositoryName, branch, sessionId, pathName);
        try (Git git = Git.cloneRepository().setURI(repositoryName).setDirectory(new File(pathName)).call()) {
            log.info("克隆仓库完成:{}", repositoryName);
            git.checkout()
                    .setName(branch)
                    .setStartPoint("origin/" + branch)
                    .setCreateBranch(true) //
                    .call();
            log.info("切换分支完成:{}", branch);
        } catch (GitAPIException e) {
            log.error("克隆仓库{}出现异常{}", repositoryName, e.getMessage(), e);
            throw new RuntimeException(e);
        }
        return pathName;
    }

    protected boolean uploadAgentFileToOOS(File file) {
        try (InputStream inputStream = new FileInputStream(file)) {
            if (0 == file.length()) {
                log.warn("待上传文件为空, 跳过上传, 文件:{}", file.getName());
                return false;
            } else {
                minioService.uploadFile(file.getName(), inputStream);
                log.info("文件上传到对象存储成功, 文件:{}", file.getName());
                return true;
            }
        } catch (Exception e) {
            log.error("上传文件{}到对象存储失败,原因:{}", file.getName(), e.getMessage(), e);
            throw new RuntimeException("上传文件失败:" + file.getName());
        }
    }

    protected String uploadAndReadAgentFileToOOS(File file) {
        try (InputStream inputStream = new FileInputStream(file)) {
            if (0 == file.length()) {
                log.warn("待上传文件为空, 返回空内容, 文件:{}", file.getName());
                return "";
            } else {
                minioService.uploadFile(file.getName(), inputStream);
                String content = FileUtil.readString(file, StandardCharsets.UTF_8);
                log.info("文件上传并读取成功, 文件:{}, 长度:{}", file.getName(), content.length());
                return content;
            }
        } catch (Exception e) {
            log.error("上传文件{}到对象存储失败,原因:{}", file.getName(), e.getMessage(), e);
            throw new RuntimeException("上传文件失败:" + file.getName());
        }
    }

    /**
     * 读取并上传jsonL文件
     *
     * @param file
     * @param clazz
     * @param <T>
     * @return
     */
    protected <T> List<T> readAndUploadAgentJsonLFileToOOS(File file, Class<T> clazz) {
        log.info("开始读取并上传jsonL文件, 文件:{}", file.getName());
        try (InputStream inputStream = new FileInputStream(file)) {
            minioService.uploadFile(file.getName(), inputStream);
        } catch (Exception e) {
            log.error("上传文件{}到对象存储失败,原因:{}", file.getName(), e.getMessage(), e);
            throw new RuntimeException("上传文件失败:" + file.getName());
        }

        List<T> result = JsonlUtil.readJsonlFile(file, clazz);
        log.info("jsonL文件读取并上传完成, 文件:{}, 记录条数:{}", file.getName(), result.size());
        return result;
    }

    /**
     * 读取并上传jsonL文件
     *
     * @param file
     * @return
     */
    protected String readAndUploadAgentJsonLFileToOOS(File file) {
        log.info("开始读取并上传jsonL文件, 文件:{}", file.getName());
        try (InputStream inputStream = new FileInputStream(file)) {
            minioService.uploadFile(file.getName(), inputStream);
        } catch (Exception e) {
            log.error("上传文件{}到对象存储失败,原因:{}", file.getName(), e.getMessage(), e);
            throw new RuntimeException("上传文件失败:" + file.getName());
        }

        String result = FileUtil.readString(file, StandardCharsets.UTF_8);
        log.info("jsonL文件读取并上传完成, 文件:{}", file.getName());
        return result;
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
        log.info("开始合并配置文件, configKey:{}, modelName:{}", configKey, modelName);
        String oosContent = minioService.getAndReadFile(configKey);
        oosContent = oosContent.replaceAll(ModelConfigConstant.API_KEY, apiKey).replaceAll(ModelConfigConstant.MODEL_NAME, modelName)
                .replaceAll(ModelConfigConstant.URL, url);
        JSONObject sourceJsonObject = JSONUtil.parseObj(sourceContent);
        JSONObject oosJsonObject = JSONUtil.parseObj(oosContent);
        JSONObject mergeResult = AgentJsonUtil.merge(sourceJsonObject, oosJsonObject);
        log.info("配置文件合并完成, configKey:{}", configKey);
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

