package com.example.agenteval.application.dto.response.record;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecordListResponse {

    /**
     * 任务名称
     */
    private String taskName;

    /**
     * agent 名称
     */
    private String agentName;

    /**
     * 模型名称
     */
    private String modelName;

    /**
     * 案例个数
     */
    private Integer caseCount;

    /**
     * 任务状态
     */
    private Integer taskStatus;

    /**
     * 评分状态
     */
    private Integer scoreStatus;

    /**
     * 任务发起人
     */
    private String taskCreateUserName;

    /**
     * 任务创建时间
     */
    private String taskCreateTaskTime;

}
