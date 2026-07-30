package com.example.agenteval.application.dto;

import com.example.agenteval.domain.model.pojo.CaseFile;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
public class StandardAnswerUploadRequest {
    @NotEmpty(message = "标准答案文件列表不能为空")
    @Valid
    private List<CaseFile> files;
}
