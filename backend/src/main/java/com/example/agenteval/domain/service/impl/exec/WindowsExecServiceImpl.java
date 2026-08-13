package com.example.agenteval.domain.service.impl.exec;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.text.StrPool;
import com.example.agenteval.domain.service.ExecService;
import com.example.agenteval.domain.service.impl.exec.condition.WindowsCondition;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service("win")
@Slf4j
@Conditional(WindowsCondition.class)
@RequiredArgsConstructor
public class WindowsExecServiceImpl extends ExecBaseAbstractService implements ExecService {

    @Override
    public String agentInvoke(String workDir, String command, String prompt, String sessionId) {

        String promptFileName = workDir + File.separator + sessionId + StrPool.UNDERLINE + "prompt.txt";
        FileUtil.writeUtf8String(prompt, promptFileName);

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
            /*File outputLog = logDir.resolve("qwen_" + sessionId + "_out.log").toFile();*/
            File errorLog = logDir.resolve("qwen_" + sessionId + "_err.log").toFile();
            // 3. 创建 ProcessBuilder
            List<String> commands = new ArrayList<>();
            commands.add("cmd.exe");
            commands.add("/c");
            Collections.addAll(commands, args);
            /*commands.add(prompt.replaceAll("\\n", "\\\\n")); // prompt 可能包含换行，但作为单个参数传递*/
            // 4. 将输出流重定向到文件，而不是由 Java 持有
            ProcessBuilder pb = new ProcessBuilder(commands);
            pb.environment().put("QWEN_CODE_SUPPRESS_YOLO_WARNING", "1");
            /*pb.redirectOutput(outputLog);*/
            pb.redirectError(errorLog);
            // 5. 设置工作目录
            pb.directory(new File(workDir));
            pb.redirectInput(new File(promptFileName));
            // 6. 启动进程
            Process process = pb.start();

            // ==== 新增：2秒健康检查 ====
            boolean exited = process.waitFor(2, TimeUnit.SECONDS);
            if (exited) {
                int exitCode = process.exitValue();
                if (exitCode != 0) {
                    String errorContent = FileUtil.readString(errorLog, Charset.forName("GBK"));
                    throw new RuntimeException("qwen 进程启动失败，退出码: " + exitCode +
                            ", 错误日志: " + errorContent);
                } else {
                    // 根据你的业务需求决定是否视为失败
                    log.warn("qwen 进程在 2 秒内正常退出（可能任务已完成或未正确启动），sessionId: {}", sessionId);
                    // 若认为这种情况也算失败，可以抛出异常（取消下面注释）
                    // throw new RuntimeException("qwen 进程过早退出，未持续运行");
                }
            } else {
                // 进程存活，启动成功
                log.info("qwen 进程已启动并持续运行，sessionId: {}", sessionId);
            }
            log.info("命令已执行，sessionId: {}, 日志将写入: {}", sessionId, logDir);
            return sessionId;
        } catch (Exception e) {
            log.error("启动 qwen 进程失败", e);
            throw new RuntimeException("启动 qwen 失败");
        }
    }
}
