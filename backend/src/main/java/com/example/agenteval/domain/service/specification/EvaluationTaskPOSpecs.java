package com.example.agenteval.domain.service.specification;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.example.agenteval.application.dto.request.record.RecordListRequest;
import com.example.agenteval.domain.model.EvaluationTaskPO;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class EvaluationTaskPOSpecs {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static Specification<EvaluationTaskPO> recordListBuildSpec(RecordListRequest request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. 任务名称模糊查询（忽略大小写，可自行调整）
            if (StrUtil.isNotBlank(request.getTaskName())) {
                predicates.add(cb.like(root.get("taskName"), "%" + request.getTaskName() + "%"));
            }

            // 2. agentId 精确匹配
            if (ObjUtil.isNotNull(request.getAgentId())) {
                predicates.add(cb.equal(root.get("agentId"), request.getAgentId()));
            }

            // 3. 任务状态 枚举 1-running,2-completed,3-cancelled）
            if (ObjUtil.isNotNull(request.getTaskStatus())) {
                predicates.add(cb.equal(root.get("status"), request.getTaskStatus()));
            }

            // 4. 开始时间 >= 传入时间（基于 create_time）
            if (StrUtil.isNotBlank(request.getStartTime())) {
                LocalDateTime start = LocalDateTime.parse(request.getStartTime(), DATE_FORMATTER);
                predicates.add(cb.greaterThanOrEqualTo(root.get("createTime"), start));
            }

            // 5. 结束时间 <= 传入时间（注意：包含当天截止）
            if (StrUtil.isNotBlank(request.getEndTime())) {
                LocalDateTime end = LocalDateTime.parse(request.getEndTime(), DATE_FORMATTER);
                predicates.add(cb.lessThanOrEqualTo(root.get("createTime"), end));
            }
            // 组装查询条件
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
