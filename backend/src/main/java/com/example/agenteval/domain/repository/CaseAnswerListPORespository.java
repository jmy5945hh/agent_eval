package com.example.agenteval.domain.repository;

import com.example.agenteval.domain.model.CaseAnswerListPO;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface CaseAnswerListPORespository extends BaseRepository<CaseAnswerListPO, Integer> {

    /**
     * 根据案例 ID 查询所有标准答案记录。
     *
     * @param caseId 案例业务 ID
     * @return 标准答案记录列表
     */
    List<CaseAnswerListPO> findByCaseId(Integer caseId);

    /**
     * 根据案例 ID 删除所有标准答案记录（全量替换时使用）。
     *
     * @param caseId 案例业务 ID
     */
    @Transactional
    void deleteByCaseId(Integer caseId);
}
