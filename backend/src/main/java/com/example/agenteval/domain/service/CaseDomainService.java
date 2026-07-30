package com.example.agenteval.domain.service;

import com.example.agenteval.application.dto.CaseCreateRequest;
import com.example.agenteval.application.dto.CaseUpdateRequest;
import com.example.agenteval.domain.model.EvaluationCasePO;
import com.example.agenteval.domain.model.pojo.CaseFile;

import java.util.List;

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
public interface CaseDomainService {

    // ==================== 案例 CRUD ====================

    /**
     * 新增案例。
     * <p>自动分配 case code（如 FE-001），将 prompt 和标准答案文件保存到对象存储。</p>
     *
     * @param request 创建请求
     * @return 创建后的案例实体
     */
    EvaluationCasePO createCase(CaseCreateRequest request);

    /**
     * 编辑案例。
     * <p>若 Prompt 或 standardAnswers 发生变更，caseVersion 自动 +1。</p>
     *
     * @param caseId  案例 ID
     * @param request 编辑请求
     * @return 更新后的案例实体
     */
    EvaluationCasePO updateCase(Long caseId, CaseUpdateRequest request);

    /**
     * 删除案例。
     * <p>先检查是否有测评任务引用该案例，有则抛出异常提示。</p>
     *
     * @param caseId 案例 ID
     */
    void deleteCase(Long caseId);

    // ==================== 标准答案管理 ====================

    /**
     * 上传/替换案例的标准答案文件列表。
     * <p>将文件保存到对象存储，更新 EvaluationCase.standardAnswerKey。</p>
     *
     * @param caseId 案例 ID
     * @param files  标准答案文件列表（全量替换）
     * @return 保存后的文件列表
     */
    List<CaseFile> saveStandardAnswers(Long caseId, List<CaseFile> files);

    // ==================== 关联查询 ====================

    /**
     * 查询案例被哪些测评任务引用。
     * <p>用于删除前的依赖检查。</p>
     *
     * @param caseId 案例 ID
     * @return 关联的任务 ID 列表（含任务简要信息）
     */
    List<String> getReferencedTaskIds(Long caseId);
}
