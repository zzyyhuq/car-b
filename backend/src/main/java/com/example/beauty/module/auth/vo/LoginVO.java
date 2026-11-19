package com.example.beauty.module.auth.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginVO {

    private String token;
    /** token 有效期，单位秒（默认 7 天 = 604800） */
    private Long expiresIn;
    private Long id;
    private String nickname;
    private String avatar;
    private String role;
}
