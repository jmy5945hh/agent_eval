package com.example.agenteval.domain.repository;

import com.example.agenteval.domain.model.ModelConfigPO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ModelConfigPORespository extends BaseRepository<ModelConfigPO, Integer> {

    /**
     * 判断指定名称的模型是否已存在，用于名称唯一性校验。
     */
    boolean existsByModelName(String modelName);

    /**
     * 根据模型名称查找。
     */
    ModelConfigPO findByModelName(String modelName);

    /**
     * 根据模型名称分页查询
     *
     * @param modelName
     * @param pageable
     * @return
     */
    @Query("SELECT m FROM ModelConfigPO m WHERE (:modelName IS NULL OR m.modelName = :modelName)")
    Page<ModelConfigPO> findByModelName(String modelName, Pageable pageable);

    /**
     * 根据模型id查询
     *
     * @param modelIds
     * @return
     */
    List<ModelConfigPO> findByIdIn(List<Integer> modelIds);
}
