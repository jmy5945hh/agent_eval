package com.example.agenteval.domain.service.impl;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.IdUtil;
import com.example.agenteval.domain.service.OSService;
import com.example.agenteval.infrastructure.config.MinioConfig;
import io.minio.*;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.MinioException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Service
@RequiredArgsConstructor
@Slf4j
public class MinioService implements OSService {

    public static MinioClient minioClient;
    private final MinioConfig minioConfig;

    @PostConstruct
    public void initializeMinio() throws Exception {
        try {
            minioClient = MinioClient.builder()
                    .endpoint(minioConfig.getEndpoint())
                    .credentials(minioConfig.getAccessKey(), minioConfig.getSecretKey())
                    .build();
        } catch (Exception e) {
            log.error("启动Minio异常，异常为:[{}]", e.getMessage(), e);
            throw new Exception("Internal server error");
        }
    }

    /**
     * 将文字作为文件上场到对象存储
     *
     * @param context
     * @return
     */
    @Override
    public String createAndUploadFile(String context) {
        String fileName = IdUtil.simpleUUID();
        uploadFile(fileName, context);
        return fileName;
    }

    /**
     * 将文字作为文件上场到对象存储
     *
     * @param context
     * @return
     */
    @Override
    public void createAndUploadFile(String fileName, String context) {
        uploadFile(fileName, context);
    }

    private void uploadFile(String fileName, String context) {
        try (InputStream inputStream = new ByteArrayInputStream(context.getBytes())) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(minioConfig.getBucket())
                            .object(fileName)
                            .stream(inputStream, inputStream.available(), -1)
                            .contentType("application/octet-stream")
                            .build());
        } catch (Exception e) {
            log.error("创建文件并上传Minio失败，异常为:[{}]", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 上传文件
     *
     * @param fileName
     * @param inputStream
     */
    @Override
    public void uploadFile(String fileName, InputStream inputStream) {
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(minioConfig.getBucket())
                            .object(fileName)
                            .stream(inputStream, inputStream.available(), -1)
                            .contentType("application/octet-stream")
                            .build());
        } catch (Exception e) {
            log.error("上传文件到Minio失败，异常为:[{}]", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取文件
     *
     * @param fileName
     */
    @Override
    public InputStream getFile(String fileName) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(minioConfig.getBucket())
                            .object(fileName)
                            .build());
        } catch (Exception e) {
            log.error("读取文件[{}]出现异常，异常为:[{}]", fileName, e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取并读取文件内容
     *
     * @param fileName
     * @return
     */
    @Override
    public String getAndReadFile(String fileName) {
        try (InputStream inputStream = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(minioConfig.getBucket())
                        .object(fileName)
                        .build())) {
            return IoUtil.read(inputStream, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("读取文件[{}]内容出现异常，异常为:[{}]", fileName, e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 删除文件
     *
     * @param fileName
     */
    @Override
    public void deleteFile(String fileName) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder().bucket(minioConfig.getBucket())
                    .object(fileName).build());
        } catch (Exception e) {
            log.error("删除文件[{}]出现异常，异常为:[{}]", fileName, e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean checkFileExist(String fileName) {
        try {
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(minioConfig.getBucket())
                            .object(fileName)
                            .build()
            );
            // 如果没有抛出异常，说明文件存在
            return true;
        } catch (ErrorResponseException e) {
            // 关键：通过错误码 "NoSuchKey" 精确判断文件不存在
            if ("NoSuchKey".equals(e.errorResponse().code())) {
                return false;
            }
            throw new RuntimeException("检查文件存在性时发生错误", e);
        } catch (MinioException | IOException | InvalidKeyException | NoSuchAlgorithmException e) {
            throw new RuntimeException("检查文件存在性时发生错误", e);
        }

    }
}
