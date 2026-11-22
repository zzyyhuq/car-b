package com.example.beauty.module.points.controller;

import com.example.beauty.common.Result;
import com.example.beauty.common.constant.Role;
import com.example.beauty.common.security.RequireRole;
import com.example.beauty.module.points.dto.PointsExchangeDTO;
import com.example.beauty.module.points.entity.PointsGoods;
import com.example.beauty.module.points.entity.PointsRecord;
import com.example.beauty.module.points.service.PointsService;
import com.example.beauty.module.points.vo.PointsExchangeVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
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

    @Operation(summary = "我的积分流水（车主）", description = "limit 默认 50、最大 100")
    @GetMapping("/record/my")
    @RequireRole({Role.OWNER})
    public Result<List<PointsRecord>> myRecords(@RequestParam(required = false) Integer limit) {
        return Result.ok(pointsService.myRecords(limit));
    }
}
