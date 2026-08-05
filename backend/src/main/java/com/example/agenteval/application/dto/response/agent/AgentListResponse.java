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
@ApiModel("查询agent返回体")
public class AgentListResponse {

    /**
     * 主键Id
     */
    @ApiModelProperty(value = "主键Id")
    private Integer id;

    /**
     * agent名称
     */
    @ApiModelProperty(value = "agent名称")
    private String agentName;

    /**
     * 默认版本
     */
    @ApiModelProperty(value = "默认版本")
    private String defaultVersion;

    /**
     * 描述
     */
    @ApiModelProperty(value = "描述")
    private String description;

    /**
     * 启动命令
     */
    @ApiModelProperty(value = "启动命令")
    private String startCmd;

    /**
     * 启用状态
     */
    @ApiModelProperty(value = "启用状态")
    private boolean enabled;

    /**
     * 配置文件路径
     */
    @ApiModelProperty(value = "配置文件路径")
    private String configPath;

    /**
     * 是否默认agent
     */
    @ApiModelProperty(value = "是否默认agent")
    private boolean defaultAgent;

    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间,格式:yyyy-MM-dd HH:mm:ss")
    private String createTime;

    /**
     * 更新时间
     */
    @ApiModelProperty(value = "修改时间,格式:yyyy-MM-dd HH:mm:ss")
    private String updateTime;

}
