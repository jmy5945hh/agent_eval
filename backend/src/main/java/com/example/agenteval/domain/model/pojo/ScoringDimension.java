package com.example.agenteval.domain.model.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScoringDimension {
    @NotBlank(message = "唯一标识不能为空")
    private String key;
    @NotBlank(message = "显示名称不能为空")
    private String label;
    @NotNull(message = "权重不能为空")
    private Integer weight;
    private String desc;
}
