package com.example.agenteval.domain.model.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecordListExcel {

    /**
     * 任务名称
     */
    @ExcelProperty("任务名称")
    private String taskName;

    /**
     * agent 名称
     */
    @ExcelProperty("Agent名称")
    private String agentName;

    /**
     * 模型名称
     */
    @ExcelProperty("模型名称")
    private String modelName;

    /**
     * 案例个数
     */
    @ExcelProperty("案例个数")
    private Integer caseCount;

    /**
     * 任务状态
     */
    @ExcelProperty("任务状态")
    private String taskStatus;

    /**
     * 评分状态
     */
    @ExcelProperty("评分状态")
    private String scoreStatus;

    /**
     * 任务发起人
     */
    @ExcelProperty("任务发起人")
    private String taskCreateUserName;

    /**
     * 任务创建时间
     */
    @ExcelProperty("任务创建时间")
    private String taskCreateTaskTime;

}
