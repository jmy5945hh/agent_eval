package com.example.agenteval.domain.service;

import com.example.agenteval.application.dto.request.cases.CaseListRequest;
import com.example.agenteval.application.dto.request.cases.CaseRequest;
import com.example.agenteval.application.dto.response.evalcase.CaseListResponse;
import org.springframework.data.domain.Page;

/**
 * 案例领域服务接口 — 负责案例的 CRUD、标准答案管理、关联任务查询。
 *
 * <h4>业务规则</h4>
 * <ul>
 *   <li>案例名称不超过 20 字。</li>
 *   <li>新增时默认 difficulty=中, category=前端, version=1。</li>
 *   <li>编辑时若 Prompt 或标准答案发生变更，版本号自动 +1。</li>
 *   <li>删除前需检查是否被测评任务引用，有引用则提示确认后标记删除。</li>
 * </ul>
 */
public interface EvaluationCaseService {

    // ==================== 案例 CRUD ====================

    /**
     * 新增案例。
     * <p>将 prompt 和标准答案文件保存到对象存储。</p>
     *
     * @param request 创建请求
     * @return 创建后的案例实体
     */
    void createCase(CaseRequest request);

    /**
     * 编辑案例。
     * <p>若 Prompt 或 standardAnswers 发生变更，caseVersion 自动 +1。</p>
     *
     * @param request 编辑请求
     * @return 更新后的案例实体
     */
    void updateCase(Integer caseId, CaseRequest request);

    /**
     * 删除案例。
     * <p>先检查是否有测评任务引用该案例，有则抛出异常提示。</p>
     *
     * @param caseId 案例 ID
     */
    void deleteCase(Integer caseId);

    /**
     * 查询案例列表。
     *
     * @param request 查询请求
     * @return 列表
     */
    Page<CaseListResponse> caseList(CaseListRequest request);
}
