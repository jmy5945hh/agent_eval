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
@Table(name = "task_case_eval_link")
public class TaskCaseEvalLinkPO extends BaseEntity {

    @Column(nullable = true, length = 100, name = "run_session_id")
    private String runSessionId;

    @Column(nullable = true, length = 100, name = "eval_session_id")
    private String evalSessionId;
}
