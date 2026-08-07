package com.example.agenteval.domain.service.mapstruct;

import com.example.agenteval.application.dto.response.infrastructure.EnumListResponse;
import com.example.agenteval.domain.model.EnumInfoPO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface EnumMapper {

    EnumListResponse toListResponse(EnumInfoPO po);

}
