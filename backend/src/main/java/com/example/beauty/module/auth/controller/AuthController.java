package com.example.beauty.module.auth.controller;

import com.example.beauty.common.Result;
import com.example.beauty.module.auth.dto.LoginDTO;
import com.example.beauty.module.auth.dto.WxLoginDTO;
import com.example.beauty.module.auth.service.AuthService;
import com.example.beauty.module.auth.vo.LoginVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "认证", description = "管理员/员工登录、车主微信登录")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "账号密码登录", description = "管理员/员工使用，返回 JWT token")
    @PostMapping("/login")
    public Result<LoginVO> login(@RequestBody @Valid LoginDTO dto) {
        return Result.ok(authService.login(dto));
    }

    @Operation(summary = "微信小程序登录", description = "用 wx.login 的 code 换取 openid 并登录（需先配置 AppID/Secret）")
    @PostMapping("/wx-login")
    public Result<LoginVO> wxLogin(@RequestBody @Valid WxLoginDTO dto) {
        return Result.ok(authService.wxLogin(dto));
    }
}
