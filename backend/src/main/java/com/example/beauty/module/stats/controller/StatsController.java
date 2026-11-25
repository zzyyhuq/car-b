package com.example.beauty.module.stats.controller;

import com.example.beauty.common.Result;
import com.example.beauty.common.constant.Role;
import com.example.beauty.common.security.RequireRole;
import com.example.beauty.module.stats.service.StatsService;
import com.example.beauty.module.stats.vo.StatsOverviewVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "数据概览", description = "管理端首页统计")
@SecurityRequirement(name = "BearerAuth")
@RestController
@RequestMapping("/api/stats")
public class StatsController {

    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    @Operation(summary = "首页统计数据（管理员）")
    @GetMapping("/overview")
    @RequireRole({Role.ADMIN})
    public Result<StatsOverviewVO> overview() {
        return Result.ok(statsService.overview());
    }
}
