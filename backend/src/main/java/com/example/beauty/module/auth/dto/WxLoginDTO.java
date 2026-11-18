package com.example.beauty.module.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class WxLoginDTO {

    /** wx.login() 拿到的临时 code，五分钟内有效且只能用一次 */
    @NotBlank(message = "缺少微信登录 code")
    private String code;
}
