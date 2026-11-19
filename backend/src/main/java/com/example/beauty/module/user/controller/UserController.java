package com.example.beauty.module.user.controller;

import com.example.beauty.common.PageResult;
import com.example.beauty.common.Result;
import com.example.beauty.common.constant.Role;
import com.example.beauty.common.security.RequireRole;
import com.example.beauty.module.user.dto.ProfileUpdateDTO;
import com.example.beauty.module.user.dto.UserQuery;
import com.example.beauty.module.user.entity.SysUser;
import com.example.beauty.module.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "用户", description = "个人资料与用户管理")
@SecurityRequirement(name = "BearerAuth")
@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "查询当前登录人资料")
    @GetMapping("/profile")
    public Result<SysUser> profile() {
        return Result.ok(userService.getProfile());
    }

    @Operation(summary = "修改当前登录人资料")
    @PutMapping("/profile")
    public Result<Void> updateProfile(@RequestBody @Valid ProfileUpdateDTO dto) {
        userService.updateProfile(dto);
        return Result.ok();
    }

    @Operation(summary = "分页查询用户（仅管理员）")
    @GetMapping("/page")
    @RequireRole({Role.ADMIN})
    public Result<PageResult<SysUser>> page(UserQuery query) {
        return Result.ok(userService.page(query));
    }

    @Operation(summary = "按 ID 查询用户（仅管理员）")
    @GetMapping("/{id}")
    @RequireRole({Role.ADMIN})
    public Result<SysUser> detail(@PathVariable Long id) {
        return Result.ok(userService.getById(id));
    }
}
