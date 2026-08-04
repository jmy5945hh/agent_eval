package com.example.agenteval.domain.service.mapstruct;

import com.example.agenteval.application.dto.response.model.ModelInfoResponse;
import com.example.agenteval.application.dto.response.model.ModelListResponse;
import com.example.agenteval.domain.model.ModelConfigPO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ModelConfigMapper {


    /**
     * 将 PO 转换为响应 DTO
     * 注意：
     * - enabled: 数据库 1 -> true, 0 -> false
     * - scoring: 数据库 1 -> true, 0 -> false
     * - createTime / updateTime: Date -> 格式 "yyyy-MM-dd HH:mm:ss"
     */
    @Mapping(target = "enabled", expression = "java(po.getEnabled() == 1)")
    @Mapping(target = "scoring", expression = "java(po.getScoring() == 1)")
    @Mapping(target = "createTime", source = "createTime", dateFormat = "yyyy-MM-dd HH:mm:ss")
    @Mapping(target = "updateTime", source = "updateTime", dateFormat = "yyyy-MM-dd HH:mm:ss")
    ModelInfoResponse toResponse(ModelConfigPO po);

    @Mapping(target = "enabled", expression = "java(po.getEnabled() == 1)")
    @Mapping(target = "scoring", expression = "java(po.getScoring() == 1)")
    @Mapping(target = "createTime", source = "createTime", dateFormat = "yyyy-MM-dd HH:mm:ss")
    @Mapping(target = "updateTime", source = "updateTime", dateFormat = "yyyy-MM-dd HH:mm:ss")
    ModelListResponse toListResponse(ModelConfigPO po);
}
