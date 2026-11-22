package com.example.beauty.module.serviceprocess.controller;

import com.example.beauty.common.Result;
import com.example.beauty.common.constant.Role;
import com.example.beauty.common.security.RequireRole;
import com.example.beauty.module.serviceprocess.dto.ServiceProcessSaveDTO;
import com.example.beauty.module.serviceprocess.service.ServiceProcessService;
import com.example.beauty.module.serviceprocess.vo.ServiceProcessVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "服务过程", description = "员工登记进度，车主查看订单时间线")
@SecurityRequirement(name = "BearerAuth")
@RestController
@RequestMapping("/api/service-process")
public class ServiceProcessController {

    private final ServiceProcessService serviceProcessService;

    public ServiceProcessController(ServiceProcessService serviceProcessService) {
        this.serviceProcessService = serviceProcessService;
    }

    @Operation(summary = "查询订单服务进度", description = "车主只能查看自己的订单")
    @GetMapping("/order/{orderId}")
    public Result<List<ServiceProcessVO>> list(@PathVariable Long orderId) {
        return Result.ok(serviceProcessService.listByOrder(orderId));
    }

    @Operation(summary = "登记服务进度（管理员/员工）")
    @PostMapping
    @RequireRole({Role.ADMIN, Role.STAFF})
    public Result<Long> add(@RequestBody @Valid ServiceProcessSaveDTO dto) {
        return Result.ok(serviceProcessService.add(dto));
    }
}
