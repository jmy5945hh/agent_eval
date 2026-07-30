package com.example.agenteval.domain.repository;

import com.example.agenteval.domain.model.EvaluationCasePO;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface EvaluationCasePORespository extends BaseRepository<EvaluationCasePO, Integer>,
        JpaSpecificationExecutor<EvaluationCasePO> {
}
