package com.example.beauty.module.order.controller;

import com.example.beauty.common.PageResult;
import com.example.beauty.common.Result;
import com.example.beauty.common.constant.Role;
import com.example.beauty.common.security.RequireRole;
import com.example.beauty.module.order.dto.OrderAdminCreateDTO;
import com.example.beauty.module.order.dto.OrderAssignDTO;
import com.example.beauty.module.order.dto.OrderCancelDTO;
import com.example.beauty.module.order.dto.OrderCreateDTO;
import com.example.beauty.module.order.dto.OrderQuery;
import com.example.beauty.module.order.dto.OrderStatusDTO;
import com.example.beauty.module.order.service.OrderService;
import com.example.beauty.module.order.vo.OrderVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "订单", description = "车主预约下单与门店订单管理")
@SecurityRequirement(name = "BearerAuth")
@RestController
@RequestMapping("/api/order")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @Operation(summary = "车主预约下单", description = "金额由后端按服务项目价格计算")
    @PostMapping
    @RequireRole({Role.OWNER})
    public Result<Long> create(@RequestBody @Valid OrderCreateDTO dto) {
        return Result.ok(orderService.create(dto));
    }

    @Operation(summary = "到店开单（管理员/员工）")
    @PostMapping("/admin-create")
    @RequireRole({Role.ADMIN, Role.STAFF})
    public Result<Long> adminCreate(@RequestBody @Valid OrderAdminCreateDTO dto) {
        return Result.ok(orderService.adminCreate(dto));
    }

    @Operation(summary = "我的订单（车主）", description = "支持按状态筛选，只返回自己的订单")
    @GetMapping("/my")
    @RequireRole({Role.OWNER})
    public Result<PageResult<OrderVO>> my(OrderQuery query) {
        return Result.ok(orderService.myPage(query));
    }

    @Operation(summary = "订单分页（管理端）", description = "支持订单号/状态/车牌/员工/日期筛选")
    @GetMapping("/page")
    @RequireRole({Role.ADMIN, Role.STAFF})
    public Result<PageResult<OrderVO>> page(OrderQuery query) {
        return Result.ok(orderService.page(query));
    }

    @Operation(summary = "订单详情", description = "车主只能查看自己的订单")
    @GetMapping("/{id}")
    public Result<OrderVO> detail(@PathVariable Long id) {
        return Result.ok(orderService.detail(id));
    }

    @Operation(summary = "派单给员工（仅管理员）", description = "待接单订单派单后自动变为已接单")
    @PutMapping("/{id}/assign")
    @RequireRole({Role.ADMIN})
    public Result<Void> assign(@PathVariable Long id, @RequestBody @Valid OrderAssignDTO dto) {
        orderService.assign(id, dto.getStaffId());
        return Result.ok();
    }

    @Operation(summary = "取消订单", description = "车主只能取消自己的订单，且仅限待接单/已接单")
    @PutMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id,
                               @RequestBody(required = false) @Valid OrderCancelDTO dto) {
        orderService.cancel(id, dto == null ? null : dto.getReason());
        return Result.ok();
    }

    @Operation(summary = "订单状态流转（管理员/员工）", description = "取消请使用取消接口")
    @PutMapping("/{id}/status")
    @RequireRole({Role.ADMIN, Role.STAFF})
    public Result<Void> updateStatus(@PathVariable Long id, @RequestBody @Valid OrderStatusDTO dto) {
        orderService.updateStatus(id, dto.getStatus());
        return Result.ok();
    }
}
