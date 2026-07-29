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
@Table(name = "evaluation_case")
public class EvaluationCasePO extends BaseEntity {


    /**
     * 案例名称,;
     */
    @Column(nullable = true, length = 50)
    private String caseName;
    /**
     * 任务提示词的对象存储 URI（如 s3://bucket/cases/C001/prompt.txt）,;
     */
    @Column(nullable = true, length = 50)
    private String promptKey;
    /**
     * Git 仓库地址,;
     */
    @Column(nullable = true, length = 200)
    private String repo;
    /**
     * Git 分支名,;
     */
    @Column(nullable = true, length = 50)
    private String branch;
    /**
     * 前端/Java后端/Python后端/AI智能体/安全测试,;
     */
    @Column(nullable = true)
    private int category;
    /**
     * 难度: 1 高 / 2 中 / 3 低,;
     */
    @Column(nullable = true)
    private int difficulty;
    /**
     * 重要性: 1 高 / 2 中 / 3 低,;
     */
    @Column(nullable = true)
    private int importance;
    /**
     * 案例版本号,;
     */
    @Column(nullable = true)
    private int caseVersion;
    /**
     * 备注说明,;
     */
    @Column(nullable = true, length = 200)
    private String remark;
    /**
     * 标准答案的文件路径,;
     */
    @Column(nullable = true, length = 100)
    private String filePath;
    /**
     * 标准答案的对象存储 URI（如 s3://bucket/cases/C001/standard_answer.json）,;
     */
    @Column(nullable = true, length = 50)
    private String standardAnswerKey;
}
