package com.example.agenteval.application.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.List;

@Data
public class CaseUpdateRequest {
    @NotBlank(message = "案例名称不能为空")
    @Size(max = 20, message = "案例名称不超过 20 字")
    private String name;

    private String code;

    @NotBlank(message = "Prompt 描述不能为空")
    private String prompt;

    @NotBlank(message = "目标仓库不能为空")
    private String repo;

    @NotBlank(message = "目标分支不能为空")
    private String branch;

    @NotBlank(message = "分类不能为空")
    private String category;

    private String difficulty;
    private String importance;
    private String remark;

    private List<StandardAnswerItem> standardAnswers;

    @Data
    public static class StandardAnswerItem {
        @NotBlank(message = "文件路径不能为空")
        private String path;

        @NotBlank(message = "文件内容不能为空")
        private String content;
    }
}
