package com.example.beauty.module.user.dto;

import lombok.Data;

@Data
public class UserQuery {

    private long page = 1;
    private long size = 10;

    /** 昵称 / 账号 / 手机号 模糊搜索 */
    private String keyword;

    private String role;

    /** 1 正常，0 禁用 */
    private Integer status;
}
