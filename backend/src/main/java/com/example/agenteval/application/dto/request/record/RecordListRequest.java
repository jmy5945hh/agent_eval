package com.example.agenteval.application.dto.request.record;

import com.example.agenteval.application.dto.BasePageRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class RecordListRequest extends BasePageRequest {

    /**
     * 任务名称
     */
    private String taskName;

    /**
     * agent 主键id
     */
    private Integer agentId;

    /**
     * 任务状态
     */
    private Integer taskStatus;

    /**
     * 开始时间
     */
    private String startTime;

    /**
     * 结束时间
     */
    private String endTime;

}
