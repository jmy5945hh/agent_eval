package com.example.agenteval.domain.service;

import com.example.agenteval.application.dto.response.infrastructure.EnumListResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface InfrastructureService {

    List<EnumListResponse> enumList(Integer enumType);

    String uploadFile(MultipartFile file);

    String readFile(String fileKey);
}
