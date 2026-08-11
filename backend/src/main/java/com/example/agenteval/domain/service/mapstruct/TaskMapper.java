package com.example.agenteval.domain.service.mapstruct;

import cn.hutool.core.lang.TypeReference;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.json.JSONUtil;
import com.example.agenteval.application.dto.response.evalcase.CaseListResponse;
import com.example.agenteval.application.dto.response.task.*;
import com.example.agenteval.domain.model.AgentInfoPO;
import com.example.agenteval.domain.model.AgentVersionPO;
import com.example.agenteval.domain.model.ModelConfigPO;
import com.example.agenteval.domain.model.ScoringStandardPO;
import com.example.agenteval.domain.model.pojo.ScoringDimension;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;
import java.util.Map;

@Mapper(componentModel = "spring")
public interface TaskMapper {


    @Mapping(target = "availableVersions", source = "id", qualifiedByName = "convertAgentVersionSize")
    TaskAgentResponse toTaskAgentResponse(AgentInfoPO agentInfoPO, @Context Map<Integer, Long> agentVersionMap);

    @Mapping(target = "agentVersion", source = "version")
    @Mapping(target = "description", source = "notes")
    TaskAgentVersionResponse toTaskAgentVersionResponse(AgentVersionPO agentVersionPO);

    TaskModelResponse toTaskModelResponse(ModelConfigPO modelConfigPO);

    @Mapping(target = "description", source = "note")
    @Mapping(target = "updateTime", source = "updateTime", dateFormat = "yyyy-MM-dd HH:mm:ss")
    @Mapping(target = "dimensions", source = "dimensions", qualifiedByName = "convertScoringDimension")
    TaskScoringStandardResponse toTaskScoringStandardResponse(ScoringStandardPO scoringStandardPO);

    TaskCaseResponse toTaskCaseResponse(CaseListResponse caseListResponse);

    @Named("convertScoringDimension")
    default List<ScoringDimension> convertDimensions(String dimensions) {
        return JSONUtil.toBean(dimensions, new TypeReference<List<ScoringDimension>>() {
        }, true);
    }

    @Named("convertAgentVersionSize")
    default Integer convertAgentVersionSize(Integer agentId, @Context Map<Integer, Long> agentVersionMap) {
        Long size = agentVersionMap.get(agentId);
        if (ObjUtil.isNull(size)) {
            return 0;
        }
        return size.intValue();
    }
}
