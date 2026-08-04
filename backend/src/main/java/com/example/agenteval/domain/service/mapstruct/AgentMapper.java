package com.example.agenteval.domain.service.mapstruct;

import com.example.agenteval.application.dto.response.agent.AgentListResponse;
import com.example.agenteval.application.dto.response.agent.AgentVersionListResponse;
import com.example.agenteval.domain.model.AgentInfoPO;
import com.example.agenteval.domain.model.AgentVersionPO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AgentMapper {

    @Mapping(target = "enabled", expression = "java(po.getEnabled() == 1)")
    @Mapping(target = "defaultAgent", expression = "java(po.getDefaultAgent() == 1)")
    @Mapping(target = "createTime", source = "createTime", dateFormat = "yyyy-MM-dd HH:mm:ss")
    @Mapping(target = "updateTime", source = "updateTime", dateFormat = "yyyy-MM-dd HH:mm:ss")
    AgentListResponse toListResponse(AgentInfoPO po);


    @Mapping(target = "enabled", expression = "java(po.getEnabled() == 1)")
    @Mapping(target = "configContent", source = "contentOsPath")
    @Mapping(target = "createTime", source = "createTime", dateFormat = "yyyy-MM-dd HH:mm:ss")
    @Mapping(target = "updateTime", source = "updateTime", dateFormat = "yyyy-MM-dd HH:mm:ss")
    AgentVersionListResponse toListResponse(AgentVersionPO po);

}
