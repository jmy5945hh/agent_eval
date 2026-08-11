package com.example.agenteval.adaptor.rest;

import com.example.agenteval.application.dto.response.CommonResponse;
import com.example.agenteval.application.dto.response.infrastructure.EnumListResponse;
import com.example.agenteval.domain.service.InfrastructureService;
import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/infrastructure")
@RequiredArgsConstructor
@Api(tags = "基础设施控制器")
@ApiSupport(order = 1)
public class InfrastructureController {

    private final InfrastructureService infrastructureService;

    @ApiOperation(value = "根据类型查询枚举列表")
    @GetMapping(path = "/enumList/{enumType}")
    public ResponseEntity<CommonResponse<List<EnumListResponse>>> enumList(@ApiParam(value = "枚举类型：1-案例类型", allowableValues = "1", required = true) @PathVariable Integer enumType) {
        return ResponseEntity.ok(CommonResponse.success(infrastructureService.enumList(enumType)));
    }

    @ApiOperation(value = "上传文件")
    @PostMapping(path = "/file/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CommonResponse<String>> uploadFile(@NotNull(message = "file不能为空") @RequestPart(value = "file") MultipartFile file) {
        return ResponseEntity.ok(CommonResponse.success(infrastructureService.uploadFile(file)));
    }

    @ApiOperation("读取对象存储文件内容")
    @GetMapping("/file/read")
    public ResponseEntity<CommonResponse<String>> readOOSFileContent(@RequestParam("filekey") String fileKey) {
        return ResponseEntity.ok(CommonResponse.success(infrastructureService.readFile(fileKey)));
    }
}
