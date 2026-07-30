package com.example.agenteval.application.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
public class AddCasesRequest {
    @NotEmpty(message = "至少选择一个案例")
    private List<String> caseIds;
}
