package com.example.beauty.module.cache.controller;

import com.example.beauty.common.Result;
import com.example.beauty.common.cache.CacheService;
import com.example.beauty.common.constant.Role;
import com.example.beauty.common.security.RequireRole;
import com.example.beauty.module.cache.vo.CacheKeyVO;
import com.example.beauty.module.cache.vo.CacheOverviewVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "缓存管理", description = "管理端查看 Redis 状态、业务缓存 key 与清理缓存")
@SecurityRequirement(name = "BearerAuth")
@RestController
@RequestMapping("/api/cache")
@RequireRole({Role.ADMIN})
public class CacheController {

    private final CacheService cacheService;

    public CacheController(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    @Operation(summary = "缓存概览（仅管理员）", description = "Redis 连接状态、版本、内存、key 数量、命中率")
    @GetMapping("/overview")
    public Result<CacheOverviewVO> overview() {
        return Result.ok(cacheService.overview());
    }

    @Operation(summary = "查询业务缓存 key（仅管理员）", description = "prefix 支持简写，如 notice；limit 默认 100、最大 500")
    @GetMapping("/keys")
    public Result<List<CacheKeyVO>> keys(@RequestParam(required = false) String prefix,
                                         @RequestParam(required = false) Integer limit) {
        return Result.ok(cacheService.keys(prefix, limit));
    }

    @Operation(summary = "删除单个业务缓存（仅管理员）")
    @DeleteMapping("/keys")
    public Result<Void> deleteKey(@RequestParam String key) {
        cacheService.deleteKey(key);
        return Result.ok();
    }

    @Operation(summary = "清空业务缓存（仅管理员）", description = "只删除 beauty: 前缀下的 key，返回删除数量")
    @DeleteMapping("/business")
    public Result<Long> clearBusiness() {
        return Result.ok(cacheService.clearBusiness());
    }
}
