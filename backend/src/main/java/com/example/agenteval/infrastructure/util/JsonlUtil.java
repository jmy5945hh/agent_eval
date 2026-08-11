package com.example.agenteval.infrastructure.util;

import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONUtil;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

/**
 * jsonL工具类
 */
public class JsonlUtil {
    /**
     * 读取jsonL文件内容工具
     *
     * @param filePath
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> List<T> readJsonlFile(File filePath, Class<T> clazz) {
        // 1. 使用 FileUtil 读取文件所有行，每行是一个 JSON 字符串[reference:0][reference:1]
        List<String> lines = FileUtil.readLines(filePath, StandardCharsets.UTF_8);
        // 2. 使用 JSONUtil 将每行 JSON 字符串转换为 Java 对象[reference:2]
        return lines.stream()
                .filter(line -> !line.trim().isEmpty()) // 过滤空行
                .map(line -> JSONUtil.toBean(line, clazz))
                .collect(Collectors.toList());
    }
}
