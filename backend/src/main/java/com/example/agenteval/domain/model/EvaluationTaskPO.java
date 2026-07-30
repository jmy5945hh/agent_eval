package com.example.agenteval.domain.model;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "evaluation_task")
public class EvaluationTaskPO extends BaseEntity {

    /**
     * 任务名称,;
     */
    @Column(nullable = true, length = 50, name = "task_name")
    private String taskName;
    /**
     * 参测 Agent 业务 ID,;
     */
    @Column(nullable = true, name = "agent_id")
    private int agentId;
    /**
     * Agent 版本业务 ID,;
     */
    @Column(nullable = true, name = "agent_version_id")
    private int agentVersionId;
    /**
     * 使用模型业务 ID,;
     */
    @Column(nullable = true, name = "model_id")
    private int modelId;
    /**
     * 创建人姓名,;
     */
    @Column(nullable = true, length = 30, name = "create_user_name")
    private String createUserName;
    /**
     * 创建人编号,;
     */
    @Column(nullable = true, length = 10, name = "create_user_id")
    private String createUserId;
    /**
     * 1-running 2-completed 3-cancelled,;
     */
    @Column(nullable = true)
    private int status;
    /**
     * 评分模型业务 ID,;
     */
    @Column(nullable = true, name = "scoring_model_id")
    private int scoringModelId;
    /**
     * 评分时使用的标准id,;
     */
    @Column(nullable = true, name = "score_standard_id")
    private int scoreStandardId;
    /**
     * 1-idle 2-scoring 3-scored,;
     */
    @Column(nullable = true, name = "scoring_status")
    private int scoringStatus;
    /**
     * 任务综合平均分（所有案例维度分汇总后的均值，评分完成后计算写入）,;
     */
    @Column(nullable = true, name = "avg_score")
    private BigDecimal avgScore;
    /**
     * 评分完成时间,;
     */
    @CreationTimestamp
    @Column(nullable = true, name = "score_time")
    private LocalDateTime scoreTime;
}
