package com.example.agenteval.domain.repository;

import com.example.agenteval.domain.model.ModelConfigPO;

public interface ModelConfigPORespository extends BaseRepository<ModelConfigPO, Integer> {

    /**
     * 判断指定名称的模型是否已存在，用于名称唯一性校验。
     */
    boolean existsByModelName(String modelName);

    /**
     * 根据模型名称查找。
     */
    ModelConfigPO findByModelName(String modelName);
}
