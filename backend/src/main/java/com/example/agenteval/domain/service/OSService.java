package com.example.agenteval.domain.service;

import java.io.InputStream;

public interface OSService {

    /**
     * 将文字作为文件上场到对象存储
     *
     * @param context
     * @return
     */
    String createAndUploadFile(String context);

    /**
     * 上传文件
     *
     * @param fileName
     * @param inputStream
     */
    void uploadFile(String fileName, InputStream inputStream);

    /**
     * 获取文件
     *
     * @param fileName
     * @return
     */
    InputStream getFile(String fileName);

    /**
     * 获取并读取文件内容
     *
     * @param fileName
     * @return
     */
    String getAndReadFile(String fileName);

    /**
     * 删除文件
     *
     * @param fileName
     */
    void deleteFile(String fileName);

    /**
     * 检查文件是否存在
     *
     * @param fileName
     * @return
     */
    boolean checkFileExist(String fileName);
}
