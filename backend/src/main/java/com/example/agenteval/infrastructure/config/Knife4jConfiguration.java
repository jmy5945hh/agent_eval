package com.example.agenteval.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

@Configuration
public class Knife4jConfiguration {

    @Bean
    public Docket createRestApi() {
        // 指定使用 Swagger2 规范
        return new Docket(DocumentationType.SWAGGER_2)
                // 添加 API 整体信息
                .apiInfo(apiInfo())
                .select()
                // 指定扫描的 Controller 包路径，替换为你的实际包名
                .apis(RequestHandlerSelectors.basePackage("com.example.agenteval.adaptor.rest"))
                .paths(PathSelectors.any())
                .build();
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("Coding Agent Evaluation Backend API 文档")
                .description("Coding Agent 评测后端服务接口文档")
                .version("1.0.0")
                .build();
    }

}
