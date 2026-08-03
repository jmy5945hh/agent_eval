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
@Table(name = "case_answer_list")
public class CaseAnswerListPO extends BaseEntity {
    /**
     * 测评案例id
     */
    @Column(nullable = false, name = "case_id")
    private Integer caseId;

    /**
     * 标准答案的文件路径
     */
    @Column(nullable = true, length = 200, name = "file_path")
    private String filePath;

    /**
     * 标准答案的对象存储 URI（如 s3://bucket/cases/C001/standard_answer.json）
     */
    @Column(nullable = true, length = 200, name = "standard_answer_key")
    private String standardAnswerKey;
}
