package com.example.agenteval.domain.service.specification;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.example.agenteval.application.dto.request.cases.CaseListRequest;
import com.example.agenteval.domain.model.EvaluationCasePO;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public class EvaluationCasePOSpecs {

    public static Specification<EvaluationCasePO> caseListBuildSpec(CaseListRequest request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StrUtil.isNotBlank(request.getCaseName())) {
                predicates.add(cb.like(root.get("caseName"), "%" + request.getCaseName() + "%"));
            }

            if (StrUtil.isNotBlank(request.getRepo())) {
                predicates.add(cb.like(root.get("repo"), "%" + request.getRepo() + "%"));
            }

            if (ObjUtil.isNotNull(request.getCategory())) {
                predicates.add(cb.equal(root.get("category"), request.getCategory()));
            }

            // 组装查询条件
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

}