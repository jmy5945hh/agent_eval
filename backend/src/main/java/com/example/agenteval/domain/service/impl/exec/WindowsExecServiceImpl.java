package com.example.agenteval.domain.service.impl.exec;

import com.example.agenteval.domain.service.ExecService;
import com.example.agenteval.domain.service.impl.exec.condition.WindowsCondition;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service("win")
@Slf4j
@Conditional(WindowsCondition.class)
@RequiredArgsConstructor
public class WindowsExecServiceImpl extends ExecBaseAbstractService implements ExecService {

    @Override
    public String agentInvoke(String workDir, String command, String prompt, String sessionId) {

        String runPrompt = MessageFormat.format(command, sessionId);
        String[] args = runPrompt.trim().split(" ");
        try {
            // 1. 定义日志输出目录（确保目录存在）
            Path logDir = Paths.get(System.getProperty("user.home"), ".qwen", "eval-logs");
            File logDirFile = logDir.toFile();
            if (!logDirFile.exists()) {
                logDirFile.mkdirs();
            }
            // 2. 标准输出和错误输出分别存到不同文件，便于排查
            File outputLog = logDir.resolve("qwen_" + sessionId + "_out.log").toFile();
            File errorLog = logDir.resolve("qwen_" + sessionId + "_err.log").toFile();
            // 3. 创建 ProcessBuilder
            List<String> commands = new ArrayList<>();
            commands.add("cmd.exe");
            commands.add("/c");
            Collections.addAll(commands, args);
            commands.add(prompt.replaceAll("\\n", "\\\\n")); // prompt 可能包含换行，但作为单个参数传递
            // 4. 将输出流重定向到文件，而不是由 Java 持有
            ProcessBuilder pb = new ProcessBuilder(commands);
            pb.redirectOutput(outputLog);
            pb.redirectError(errorLog);
            // 5. 设置工作目录
            pb.directory(new File(workDir));
            // 6. 启动进程
            pb.start();
            log.info("命令已执行，sessionId: {}, 日志将写入: {}", sessionId, logDir);
            return sessionId;
        } catch (IOException e) {
            log.error("启动 qwen 进程失败", e);
            throw new RuntimeException("启动 qwen 失败");
        }
    }
}
