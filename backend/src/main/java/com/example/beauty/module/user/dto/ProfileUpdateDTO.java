package com.example.beauty.module.user.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProfileUpdateDTO {

    @Size(max = 50, message = "昵称最多 50 个字")
    private String nickname;

    @Size(max = 20, message = "手机号最多 20 位")
    private String phone;

    @Size(max = 255, message = "头像地址过长")
    private String avatar;
}
