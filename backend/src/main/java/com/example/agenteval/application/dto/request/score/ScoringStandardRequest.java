package com.example.agenteval.application.dto.request.score;

import com.example.agenteval.domain.model.pojo.ScoringDimension;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
@ApiModel("评分标准请求体")
public class ScoringStandardRequest {
    @NotBlank(message = "版本号不能为空")
    @ApiModelProperty(value = "版本号", required = true)
    private String version;

    @ApiModelProperty(value = "是否为当前默认版本")
    private Boolean currentVersion;

    @ApiModelProperty(value = "版本说明")
    private String note;

    @NotEmpty(message = "至少需要一个评分维度")
    @Valid
    @ApiModelProperty(value = "评分维度列表", required = true)
    private List<ScoringDimension> dimensions;
}
