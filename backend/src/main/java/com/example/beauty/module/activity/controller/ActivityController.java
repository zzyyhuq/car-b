package com.example.beauty.module.activity.controller;

import com.example.beauty.common.PageResult;
import com.example.beauty.common.Result;
import com.example.beauty.common.constant.Role;
import com.example.beauty.common.security.RequireRole;
import com.example.beauty.module.activity.dto.ActivityQuery;
import com.example.beauty.module.activity.dto.ActivitySaveDTO;
import com.example.beauty.module.activity.entity.Activity;
import com.example.beauty.module.activity.service.ActivityService;
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

@Tag(name = "活动", description = "小程序首页活动与管理端活动管理")
@SecurityRequirement(name = "BearerAuth")
@RestController
@RequestMapping("/api/activity")
public class ActivityController {

    private final ActivityService activityService;

    public ActivityController(ActivityService activityService) {
        this.activityService = activityService;
    }

    @Operation(summary = "查询进行中的活动（车主端）", description = "按开始时间倒序，limit 默认 5、最大 20")
    @GetMapping("/list")
    public Result<List<Activity>> list(@RequestParam(required = false) Integer limit) {
        return Result.ok(activityService.listOngoing(limit));
    }

    @Operation(summary = "分页查询活动（管理端）")
    @GetMapping("/page")
    @RequireRole({Role.ADMIN})
    public Result<PageResult<Activity>> page(ActivityQuery query) {
        return Result.ok(activityService.page(query));
    }

    @Operation(summary = "活动详情", description = "车主只能看进行中的活动")
    @GetMapping("/{id}")
    public Result<Activity> detail(@PathVariable Long id) {
        return Result.ok(activityService.getById(id));
    }

    @Operation(summary = "新增活动（管理员）")
    @PostMapping
    @RequireRole({Role.ADMIN})
    public Result<Long> create(@RequestBody @Valid ActivitySaveDTO dto) {
        return Result.ok(activityService.create(dto));
    }

    @Operation(summary = "修改活动（管理员）")
    @PutMapping("/{id}")
    @RequireRole({Role.ADMIN})
    public Result<Void> update(@PathVariable Long id, @RequestBody @Valid ActivitySaveDTO dto) {
        activityService.update(id, dto);
        return Result.ok();
    }

    @Operation(summary = "上线/下线活动（管理员）", description = "status：1 上线，0 下线")
    @PutMapping("/{id}/status")
    @RequireRole({Role.ADMIN})
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        activityService.updateStatus(id, status);
        return Result.ok();
    }

    @Operation(summary = "删除活动（管理员）")
    @DeleteMapping("/{id}")
    @RequireRole({Role.ADMIN})
    public Result<Void> delete(@PathVariable Long id) {
        activityService.delete(id);
        return Result.ok();
    }
}
