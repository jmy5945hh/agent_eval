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
@Table(name = "task_case_score")
public class TaskCaseScorePO extends BaseEntity {

    /**
     * 关联执行记录业务 ID,;
     */
    @Column(nullable = true)
    private int runId;
    /**
     * 维度 key（如 correctness）,;
     */
    @Column(nullable = true, length = 50)
    private String dimKey;
    /**
     * 维度名称（如 正确性）,;
     */
    @Column(nullable = true, length = 50)
    private String dimLabel;
    /**
     * 维度得分（0-100）,;
     */
    @Column(nullable = true)
    private int score;
    /**
     * 维度评语,;
     */
    @Column(nullable = true, length = 200)
    private String comment;
}
