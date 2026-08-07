package com.example.agenteval.domain.service.mapstruct;

import cn.hutool.core.util.ObjUtil;
import com.example.agenteval.application.dto.response.evalcase.CaseListResponse;
import com.example.agenteval.domain.model.EnumInfoPO;
import com.example.agenteval.domain.model.EvaluationCasePO;
import com.example.agenteval.infrastructure.enums.DifficultyEnum;
import com.example.agenteval.infrastructure.enums.ImportanceEnum;
import com.example.agenteval.infrastructure.util.EnumUtil;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Map;

@Mapper(componentModel = "spring")
public interface EvaluationCaseMapper {

    @Mapping(target = "difficulty", source = "difficulty", qualifiedByName = "convertDifficulty")
    @Mapping(target = "importance", source = "importance", qualifiedByName = "convertImportance")
    @Mapping(target = "category", source = "category", qualifiedByName = "convertCategory")
    @Mapping(target = "updateTime", source = "updateTime", dateFormat = "yyyy-MM-dd HH:mm:ss")
    CaseListResponse toCaseListResponse(EvaluationCasePO evaluationCasePO, @Context Map<Integer, EnumInfoPO> contextMap);

    @Named("convertCategory")
    default String convertCategory(String categoryStr, @Context Map<Integer, EnumInfoPO> contextMap) {
        if (categoryStr == null) {
            return null;
        }
        EnumInfoPO enumInfoPO = contextMap.get(Integer.valueOf(categoryStr));
        if (ObjUtil.isNull(enumInfoPO)) {
            return null;
        }
        return enumInfoPO.getEnumValue();
    }

    @Named("convertDifficulty")
    default String convertDifficulty(String difficultyStr) {
        if (difficultyStr == null) {
            return null;
        }
        try {
            DifficultyEnum enumVal = EnumUtil.findEnumByField(DifficultyEnum.class, DifficultyEnum.DIFFICULTY_NAME, Integer.valueOf(difficultyStr));
            return enumVal != null ? enumVal.getInterpretation() : null;
        } catch (NumberFormatException e) {
            // 处理非数字字符串的情况，按需返回默认值或抛出异常
            return null;
        }
    }

    @Named("convertImportance")
    default String convertImportance(String importanceStr) {
        if (importanceStr == null) {
            return null;
        }
        try {
            ImportanceEnum enumVal = EnumUtil.findEnumByField(ImportanceEnum.class, ImportanceEnum.IMPORTANCE_NAME, Integer.valueOf(importanceStr));
            return enumVal != null ? enumVal.getInterpretation() : null;
        } catch (NumberFormatException e) {
            // 处理非数字字符串的情况，按需返回默认值或抛出异常
            return null;
        }
    }

}

