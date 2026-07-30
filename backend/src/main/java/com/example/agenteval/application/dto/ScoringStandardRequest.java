package com.example.agenteval.application.dto;

import com.example.agenteval.domain.model.pojo.ScoringDimension;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
public class ScoringStandardRequest {
    @NotBlank(message = "版本号不能为空")
    private String version;

    private Boolean isCurrent;

    private String note;

    @NotEmpty(message = "至少需要一个评分维度")
    @Valid
    private List<ScoringDimension> dimensions;
}
