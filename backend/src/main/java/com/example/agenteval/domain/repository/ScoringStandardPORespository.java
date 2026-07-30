package com.example.agenteval.domain.repository;

import com.example.agenteval.domain.model.ScoringStandardPO;

import java.util.List;

public interface ScoringStandardPORespository extends BaseRepository<ScoringStandardPO, Integer> {

    /**
     * 判断指定版本号是否已存在。
     */
    boolean existsByVersion(String version);

    /**
     * 根据版本号查找。
     */
    ScoringStandardPO findByVersion(String version);

    /**
     * 查找所有标记为当前版本的评分标准。
     */
    List<ScoringStandardPO> findByIsCurrent(byte isCurrent);
}
