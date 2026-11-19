package com.example.beauty.module.servicetype.controller;

import com.example.beauty.common.PageResult;
import com.example.beauty.common.Result;
import com.example.beauty.common.constant.Role;
import com.example.beauty.common.security.RequireRole;
import com.example.beauty.module.servicetype.dto.ServiceTypeQuery;
import com.example.beauty.module.servicetype.dto.ServiceTypeSaveDTO;
import com.example.beauty.module.servicetype.entity.ServiceType;
import com.example.beauty.module.servicetype.service.ServiceTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "服务项目", description = "美容服务项目的增删改查与上下架")
@SecurityRequirement(name = "BearerAuth")
@RestController
@RequestMapping("/api/service-type")
public class ServiceTypeController {

    private final ServiceTypeService serviceTypeService;

    public ServiceTypeController(ServiceTypeService serviceTypeService) {
        this.serviceTypeService = serviceTypeService;
    }

    /** 管理端分页 */
    @Operation(summary = "分页查询服务项目（管理端）")
    @GetMapping("/page")
    @RequireRole({Role.ADMIN})
    public Result<PageResult<ServiceType>> page(ServiceTypeQuery query) {
        return Result.ok(serviceTypeService.page(query));
    }

    /** 车主端/员工端：只看上架项目 */
    @Operation(summary = "查询上架中的服务项目（车主端）")
    @GetMapping("/list")
    public Result<List<ServiceType>> list() {
        return Result.ok(serviceTypeService.listOnSale());
    }

    @Operation(summary = "查询服务项目详情")
    @GetMapping("/{id}")
    public Result<ServiceType> detail(@PathVariable Long id) {
        return Result.ok(serviceTypeService.getById(id));
    }

    @Operation(summary = "新增服务项目（仅管理员）")
    @PostMapping
    @RequireRole({Role.ADMIN})
    public Result<Void> create(@RequestBody @Valid ServiceTypeSaveDTO dto) {
        serviceTypeService.create(dto);
        return Result.ok();
    }

    @Operation(summary = "修改服务项目（仅管理员）")
    @PutMapping("/{id}")
    @RequireRole({Role.ADMIN})
    public Result<Void> update(@PathVariable Long id, @RequestBody @Valid ServiceTypeSaveDTO dto) {
        serviceTypeService.update(id, dto);
        return Result.ok();
    }

    /** 上下架 */
    @Operation(summary = "上架/下架（仅管理员）", description = "status：1 上架，0 下架")
    @PutMapping("/{id}/status")
    @RequireRole({Role.ADMIN})
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        serviceTypeService.updateStatus(id, status);
        return Result.ok();
    }

    /** 逻辑删除，订单里引用的项目不会消失 */
    @Operation(summary = "删除服务项目（仅管理员）", description = "逻辑删除，历史订单中引用的项目不受影响")
    @DeleteMapping("/{id}")
    @RequireRole({Role.ADMIN})
    public Result<Void> delete(@PathVariable Long id) {
        serviceTypeService.delete(id);
        return Result.ok();
    }
}
