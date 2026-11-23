package com.example.beauty.module.points.controller;

import com.example.beauty.common.PageResult;
import com.example.beauty.common.Result;
import com.example.beauty.common.constant.Role;
import com.example.beauty.common.security.RequireRole;
import com.example.beauty.module.points.dto.PointsExchangeDTO;
import com.example.beauty.module.points.dto.PointsExchangeQuery;
import com.example.beauty.module.points.dto.PointsGoodsQuery;
import com.example.beauty.module.points.dto.PointsGoodsSaveDTO;
import com.example.beauty.module.points.entity.PointsGoods;
import com.example.beauty.module.points.entity.PointsRecord;
import com.example.beauty.module.points.service.PointsService;
import com.example.beauty.module.points.vo.PointsExchangeVO;
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

@Tag(name = "积分", description = "积分商品、兑换与积分流水")
@SecurityRequirement(name = "BearerAuth")
@RestController
@RequestMapping("/api/points")
public class PointsController {

    private final PointsService pointsService;

    public PointsController(PointsService pointsService) {
        this.pointsService = pointsService;
    }

    @Operation(summary = "查询上架的积分商品（车主端）")
    @GetMapping("/goods/list")
    public Result<List<PointsGoods>> goods() {
        return Result.ok(pointsService.listOnSale());
    }

    @Operation(summary = "分页查询积分商品（管理端）")
    @GetMapping("/goods/page")
    @RequireRole({Role.ADMIN})
    public Result<PageResult<PointsGoods>> goodsPage(PointsGoodsQuery query) {
        return Result.ok(pointsService.pageGoods(query));
    }

    @Operation(summary = "新增积分商品（管理员）")
    @PostMapping("/goods")
    @RequireRole({Role.ADMIN})
    public Result<Long> createGoods(@RequestBody @Valid PointsGoodsSaveDTO dto) {
        return Result.ok(pointsService.createGoods(dto));
    }

    @Operation(summary = "修改积分商品（管理员）")
    @PutMapping("/goods/{id}")
    @RequireRole({Role.ADMIN})
    public Result<Void> updateGoods(@PathVariable Long id,
                                    @RequestBody @Valid PointsGoodsSaveDTO dto) {
        pointsService.updateGoods(id, dto);
        return Result.ok();
    }

    @Operation(summary = "上架/下架积分商品（管理员）", description = "status：1 上架，0 下架")
    @PutMapping("/goods/{id}/status")
    @RequireRole({Role.ADMIN})
    public Result<Void> updateGoodsStatus(@PathVariable Long id, @RequestParam Integer status) {
        pointsService.updateGoodsStatus(id, status);
        return Result.ok();
    }

    @Operation(summary = "删除积分商品（管理员）", description = "逻辑删除，商品不再展示；历史兑换单记录保留")
    @DeleteMapping("/goods/{id}")
    @RequireRole({Role.ADMIN})
    public Result<Void> deleteGoods(@PathVariable Long id) {
        pointsService.deleteGoods(id);
        return Result.ok();
    }

    @Operation(summary = "兑换积分商品（车主）", description = "扣积分、扣库存、写兑换单与积分流水在同一事务内")
    @PostMapping("/exchange")
    @RequireRole({Role.OWNER})
    public Result<PointsExchangeVO> exchange(@RequestBody @Valid PointsExchangeDTO dto) {
        return Result.ok(pointsService.exchange(dto.getGoodsId()));
    }

    @Operation(summary = "我的兑换记录（车主）")
    @GetMapping("/exchange/my")
    @RequireRole({Role.OWNER})
    public Result<List<PointsExchangeVO>> myExchanges(@RequestParam(required = false) Integer limit) {
        return Result.ok(pointsService.myExchanges(limit));
    }

    @Operation(summary = "分页查询兑换单（管理端）", description = "支持兑换单号/状态/用户/商品筛选")
    @GetMapping("/exchange/page")
    @RequireRole({Role.ADMIN, Role.STAFF})
    public Result<PageResult<PointsExchangeVO>> exchangePage(PointsExchangeQuery query) {
        return Result.ok(pointsService.pageExchanges(query));
    }

    @Operation(summary = "核销兑换单（管理员/门店员工）", description = "待核销 → 已核销")
    @PutMapping("/exchange/{id}/verify")
    @RequireRole({Role.ADMIN, Role.STAFF})
    public Result<Void> verifyExchange(@PathVariable Long id) {
        pointsService.verifyExchange(id);
        return Result.ok();
    }

    @Operation(summary = "我的积分流水（车主）", description = "limit 默认 50、最大 100")
    @GetMapping("/record/my")
    @RequireRole({Role.OWNER})
    public Result<List<PointsRecord>> myRecords(@RequestParam(required = false) Integer limit) {
        return Result.ok(pointsService.myRecords(limit));
    }
}
