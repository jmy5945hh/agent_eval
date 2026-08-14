package com.example.agenteval.domain.service.specification;

import cn.hutool.core.util.ObjUtil;
import com.example.agenteval.domain.model.TaskCaseRunPO;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public class TaskCaseRunPOSpecs {

    public static Specification<TaskCaseRunPO> taskCaseListBuildSpec(Integer taskId, Integer state) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("taskId"), taskId));

            if (ObjUtil.isNotNull(state)) {
                predicates.add(cb.equal(root.get("status"), state));
            }

            // 组装查询条件
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
