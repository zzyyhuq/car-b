package com.example.beauty.module.user.controller;

import com.example.beauty.common.PageResult;
import com.example.beauty.common.Result;
import com.example.beauty.common.constant.Role;
import com.example.beauty.common.security.RequireRole;
import com.example.beauty.module.user.dto.ProfileUpdateDTO;
import com.example.beauty.module.user.dto.UserQuery;
import com.example.beauty.module.user.entity.SysUser;
import com.example.beauty.module.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    public Result<SysUser> profile() {
        return Result.ok(userService.getProfile());
    }

    @PutMapping("/profile")
    public Result<Void> updateProfile(@RequestBody @Valid ProfileUpdateDTO dto) {
        userService.updateProfile(dto);
        return Result.ok();
    }

    @GetMapping("/page")
    @RequireRole({Role.ADMIN})
    public Result<PageResult<SysUser>> page(UserQuery query) {
        return Result.ok(userService.page(query));
    }

    @GetMapping("/{id}")
    @RequireRole({Role.ADMIN})
    public Result<SysUser> detail(@PathVariable Long id) {
        return Result.ok(userService.getById(id));
    }
}
