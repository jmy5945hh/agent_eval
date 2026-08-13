package com.example.agenteval.domain.service.mapstruct;

import com.example.agenteval.domain.model.pojo.ScoreCommentResult;
import com.example.agenteval.domain.model.pojo.ScoringDimension;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ScoreCommentResultMapper {

    @Mapping(target = "score", constant = "0")
    @Mapping(target = "comment", constant = "")
    ScoreCommentResult toDimensionsResult(ScoringDimension scoringDimension);

}
