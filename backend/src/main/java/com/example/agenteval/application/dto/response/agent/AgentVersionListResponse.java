package com.example.agenteval.application.dto.response.agent;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ApiModel("查询Agent版本返回体")
public class AgentVersionListResponse {
    /**
     * 主键id
     */
    @ApiModelProperty(value = "主键id")
    private Integer id;

    /**
     * agentId
     */
    @ApiModelProperty(value = "agentId")
    private Integer agentId;

    /**
     * 版本
     */
    @ApiModelProperty(value = "版本")
    private String version;

    /**
     * 版本说明
     */
    @ApiModelProperty(value = "版本说明")
    private String notes;

    /**
     * 启用开关
     */
    @ApiModelProperty(value = "启用开关")
    private boolean enabled;

    /**
     * 配置内容
     */
    @ApiModelProperty(value = "配置内容")
    private String configContent;

    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间,格式：yyyy-MM-dd HH:mm:ss")
    private String createTime;

    /**
     * 更新时间
     */
    @ApiModelProperty(value = "更新时间,格式：yyyy-MM-dd HH:mm:ss")
    private String updateTime;

}
