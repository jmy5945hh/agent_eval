package com.example.agenteval.application.dto.response.score;

import com.example.agenteval.domain.model.pojo.ScoringDimension;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ApiModel("评分标准列表返回体")
public class ScoringStandardListResponse {

    @ApiModelProperty(value = "评分标准id")
    private Integer id;

    @ApiModelProperty(value = "版本号")
    private String version;

    @ApiModelProperty(value = "是否为当前默认版本")
    private Boolean currentVersion;

    @ApiModelProperty(value = "版本说明")
    private String note;

    @ApiModelProperty(value = "评分维度列表")
    private List<ScoringDimension> dimensions;

}
