package com.example.beauty.module.activitysignup.controller;

import com.example.beauty.common.Result;
import com.example.beauty.common.constant.Role;
import com.example.beauty.common.security.RequireRole;
import com.example.beauty.module.activitysignup.service.ActivitySignupService;
import com.example.beauty.module.activitysignup.vo.ActivitySignupVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "活动报名", description = "需要报名的活动：车主报名/取消，管理端查看名单")
@SecurityRequirement(name = "BearerAuth")
@RestController
@RequestMapping("/api/activity")
public class ActivitySignupController {

    private final ActivitySignupService signupService;

    public ActivitySignupController(ActivitySignupService signupService) {
        this.signupService = signupService;
    }

    @Operation(summary = "报名活动（车主）", description = "只有 needSignup=1 的进行中活动可以报名")
    @PostMapping("/{id}/signup")
    @RequireRole({Role.OWNER})
    public Result<Void> signup(@PathVariable Long id) {
        signupService.signup(id);
        return Result.ok();
    }

    @Operation(summary = "取消报名（车主）")
    @PutMapping("/{id}/signup/cancel")
    @RequireRole({Role.OWNER})
    public Result<Void> cancel(@PathVariable Long id) {
        signupService.cancel(id);
        return Result.ok();
    }

    @Operation(summary = "我的报名状态（车主）", description = "没有报名记录时 data 为 null")
    @GetMapping("/{id}/signup/my")
    @RequireRole({Role.OWNER})
    public Result<ActivitySignupVO> mySignup(@PathVariable Long id) {
        return Result.ok(signupService.mySignup(id));
    }

    @Operation(summary = "查看报名名单（管理员）")
    @GetMapping("/{id}/signups")
    @RequireRole({Role.ADMIN})
    public Result<List<ActivitySignupVO>> list(@PathVariable Long id) {
        return Result.ok(signupService.listByActivity(id));
    }
}
