package com.example.agenteval.domain.model.pojo;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ScoreCommentResult extends ScoringDimension {

    @ApiModelProperty("分数")
    private Integer score = 0;

    @ApiModelProperty("评论")
    private String comment = "";

}
