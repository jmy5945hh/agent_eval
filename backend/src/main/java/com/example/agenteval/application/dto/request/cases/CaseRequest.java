package com.example.agenteval.application.dto.request.cases;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@Data
@ApiModel(description = "案例请求体")
public class CaseRequest {
    @NotBlank(message = "案例名称不能为空")
    @Size(max = 50, message = "案例名称不超过 20 字")
    @ApiModelProperty(value = "案例名称", required = true)
    private String caseName;

    @NotBlank(message = "Prompt 描述不能为空")
    @ApiModelProperty(value = "Prompt 描述", required = true)
    private String prompt;

    @NotBlank(message = "目标仓库不能为空")
    @ApiModelProperty(value = "目标仓库", required = true)
    private String repo;

    @NotBlank(message = "目标分支不能为空")
    @ApiModelProperty(value = "目标分支", required = true)
    private String branch;

    @NotNull(message = "分类不能为空")
    @ApiModelProperty(value = "分类", required = true, allowableValues = "调用枚举接口，类型传“1”获取主键id")
    private Integer category;

    /**
     * 难度
     */
    @ApiModelProperty(value = "难度,默认1", allowableValues = "1-高,2-中,3-低")
    private Integer difficulty = 1;
    /**
     * 重要性
     */
    @ApiModelProperty(value = "重要性,默认1", allowableValues = "1-高,2-中,3-低")
    private Integer importance = 1;

    @ApiModelProperty(value = "备注")
    private String remark = "";

    @ApiModelProperty(value = "标准答案")
    private List<StandardAnswerItem> standardAnswers;

    @ApiModel(description = "标准答案项请求体")
    @Data
    public static class StandardAnswerItem {
        @ApiModelProperty(value = "文件路径")
        @NotBlank(message = "文件路径不能为空")
        private String path;

        @ApiModelProperty(value = "文件唯一标识")
        @NotBlank(message = "文件唯一标识")
        private String fileKey;
    }
}
