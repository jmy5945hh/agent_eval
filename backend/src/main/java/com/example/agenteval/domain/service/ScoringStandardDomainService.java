package com.example.agenteval.domain.service;

import com.example.agenteval.application.dto.ScoringStandardRequest;
import com.example.agenteval.domain.model.ScoringStandardPO;

/**
 * 评分标准领域服务接口 — 负责评分标准的版本管理。
 *
 * <h4>业务规则</h4>
 * <ul>
 *   <li>版本号唯一（如 v1.0、v2.0）。</li>
 *   <li>同一时间只有一个 isCurrent=true 的版本。</li>
 *   <li>所有维度权重之和必须为 100%。</li>
 *   <li>设为当前版本时，其他版本的 isCurrent 自动置为 false。</li>
 * </ul>
 */
public interface ScoringStandardDomainService {

    /**
     * 新增评分标准版本。
     * <p>若 isCurrent=true，需先将其他版本置为非当前。</p>
     *
     * @param request 包含 version、note、dimensions
     * @return 创建后的评分标准实体
     */
    ScoringStandardPO createStandard(ScoringStandardRequest request);

    /**
     * 编辑评分标准版本。
     * <p>版本号不可修改；只允许修改 note 和 dimensions。</p>
     *
     * @param id      评分标准 ID
     * @param request 编辑请求
     * @return 更新后的评分标准实体
     */
    ScoringStandardPO updateStandard(Long id, ScoringStandardRequest request);

    /**
     * 删除评分标准版本。
     * <p>检查是否被测评任务引用，有则提示不允许删除。</p>
     *
     * @param id 评分标准 ID
     */
    void deleteStandard(Long id);
}
