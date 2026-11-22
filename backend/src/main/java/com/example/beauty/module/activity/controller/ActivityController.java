package com.example.beauty.module.activity.controller;

import com.example.beauty.common.Result;
import com.example.beauty.module.activity.entity.Activity;
import com.example.beauty.module.activity.service.ActivityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "活动", description = "小程序首页活动")
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
}
