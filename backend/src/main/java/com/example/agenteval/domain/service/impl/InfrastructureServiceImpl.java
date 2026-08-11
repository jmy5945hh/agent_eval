package com.example.agenteval.domain.service.impl;

import cn.hutool.core.util.IdUtil;
import com.example.agenteval.application.dto.response.infrastructure.EnumListResponse;
import com.example.agenteval.domain.model.EnumInfoPO;
import com.example.agenteval.domain.repository.EnumInfoPORespository;
import com.example.agenteval.domain.service.InfrastructureService;
import com.example.agenteval.domain.service.mapstruct.EnumMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class InfrastructureServiceImpl implements InfrastructureService {

    private final MinioService minioService;
    private final EnumInfoPORespository enumInfoPORespository;
    private final EnumMapper enumMapper;

    @Override
    public List<EnumListResponse> enumList(Integer enumType) {
        List<EnumInfoPO> enumInfoPOS = enumInfoPORespository.findAllByEnumType(enumType);
        return enumInfoPOS.stream().map(enumMapper::toListResponse).collect(Collectors.toList());
    }

    @Override
    public String uploadFile(MultipartFile file) {
        String fileName = IdUtil.simpleUUID();
        try {
            minioService.uploadFile(fileName, file.getInputStream());
        } catch (IOException e) {
            throw new RuntimeException("上传文件失败:" + e.getMessage(), e);
        }
        return fileName;
    }

    @Override
    public String readFile(String fileKey) {
        try {
            return minioService.getAndReadFile(fileKey);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
