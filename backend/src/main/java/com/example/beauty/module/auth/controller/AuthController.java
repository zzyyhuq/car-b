package com.example.beauty.module.auth.controller;

import com.example.beauty.common.Result;
import com.example.beauty.module.auth.dto.LoginDTO;
import com.example.beauty.module.auth.dto.WxLoginDTO;
import com.example.beauty.module.auth.service.AuthService;
import com.example.beauty.module.auth.vo.LoginVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public Result<LoginVO> login(@RequestBody @Valid LoginDTO dto) {
        return Result.ok(authService.login(dto));
    }

    @PostMapping("/wx-login")
    public Result<LoginVO> wxLogin(@RequestBody @Valid WxLoginDTO dto) {
        return Result.ok(authService.wxLogin(dto));
    }
}
