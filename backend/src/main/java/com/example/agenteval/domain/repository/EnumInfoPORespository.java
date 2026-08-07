package com.example.agenteval.domain.repository;

import com.example.agenteval.domain.model.EnumInfoPO;

import java.util.List;

public interface EnumInfoPORespository extends BaseRepository<EnumInfoPO, Integer> {

    /**
     * 根据主键id和类型查询
     *
     * @param id
     * @param EnumType
     * @return
     */
    EnumInfoPO findAllByIdAndEnumType(Integer id, Integer EnumType);

    /**
     * 根据类型查询
     */
    List<EnumInfoPO> findAllByEnumType(Integer enumType);
}
