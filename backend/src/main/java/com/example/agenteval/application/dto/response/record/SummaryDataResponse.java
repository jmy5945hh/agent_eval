package com.example.agenteval.application.dto.response.record;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SummaryDataResponse {

    /**
     * 全部任务
     */
    private Integer taskCount;

    /**
     * 完成任务
     */
    private Integer finishCount;

    /**
     * 执行中的任务
     */
    private Integer runCount;

    /**
     * 队列中的案例
     */
    private Integer queueCases;

    /**
     * 平均得分
     */
    private double averageScore;


}
