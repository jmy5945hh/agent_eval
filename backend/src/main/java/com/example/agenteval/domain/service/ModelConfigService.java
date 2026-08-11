package com.example.agenteval.domain.service;

import com.example.agenteval.application.dto.request.model.ModelConfigRequest;
import com.example.agenteval.application.dto.request.model.ModelListRequest;
import com.example.agenteval.application.dto.response.model.ModelInfoResponse;
import com.example.agenteval.application.dto.response.model.ModelListResponse;
import com.example.agenteval.domain.model.ModelConfigPO;
import org.springframework.data.domain.Page;

/**
 * 模型配置领域服务接口 — 负责模型的新增、修改、删除。
 *
 * <h4>业务规则</h4>
 * <ul>
 *   <li>模型分为测评模型（scoring=false）和评分模型（scoring=true）。</li>
 *   <li>删除模型前检查是否被测评任务或评分任务引用。</li>
 *   <li>API Key 需加密存储。</li>
 * </ul>
 */
public interface ModelConfigService {

    /**
     * 新增模型配置。
     *
     * @param request 包含 name、endpoint、authorization、scoring 等
     * @return 创建后的模型实体
     */
    ModelConfigPO createModel(ModelConfigRequest request);

    /**
     * 编辑模型配置。
     *
     * @param id      模型 ID
     * @param request 编辑请求
     * @return 更新后的模型实体
     */
    ModelConfigPO updateModel(Integer id, ModelConfigRequest request);

    /**
     * 删除模型配置。
     * <p>检查是否被任务引用，有则提示不允许删除。</p>
     *
     * @param id 模型 ID
     */
    void deleteModel(Integer id);

    /**
     * 根据模型名称分页查询
     *
     * @param request
     * @return
     */
    Page<ModelListResponse> modelList(ModelListRequest request);

    /**
     * 根据id查询模型信息
     *
     * @param id
     * @return
     */
    ModelInfoResponse modelInfo(Integer id);
}
