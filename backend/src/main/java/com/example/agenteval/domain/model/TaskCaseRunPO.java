package com.example.agenteval.domain.model;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "task_case_run")
public class TaskCaseRunPO extends BaseEntity {

    /**
     * 所属任务业务 ID,;
     */
    @Column(nullable = true)
    private int taskId;
    /**
     * 测评案例业务 ID,;
     */
    @Column(nullable = true)
    private int caseId;
    /**
     * 1-queued 2-running 3-success 4-failed 5-cancelled,;
     */
    @Column(nullable = true)
    private int status;
    /**
     * 重试次数,;
     */
    @Column(nullable = true)
    private int attempts;
    /**
     * 执行轮次（agent-user 交互轮数）,;
     */
    @Column(nullable = true)
    private int rounds;
    /**
     * 输入 Token 数,;
     */
    @Column(nullable = true)
    private int tokensIn;
    /**
     * 输出 Token 数,;
     */
    @Column(nullable = true)
    private int tokensOut;
    /**
     * 执行耗时（毫秒）,;
     */
    @Column(nullable = true)
    private long durationMs;
    /**
     * 错误信息对象存储键值,;
     */
    @Column(nullable = true, length = 50)
    private String errorInfoKey;
    /**
     * 执行轨迹对象存储键值,;
     */
    @Column(nullable = true, length = 50)
    private String trajectoryKey;
}
