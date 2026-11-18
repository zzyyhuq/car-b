package com.example.beauty.module.auth.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginVO {

    private String token;
    private Long id;
    private String nickname;
    private String avatar;
    private String role;
}
