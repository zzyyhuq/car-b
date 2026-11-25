package com.example.beauty.module.activitysignup.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ActivitySignupVO {

    private Long id;
    private Long activityId;
    private Long userId;
    private String userName;
    private String phone;
    private Integer status;
    private String statusText;
    private LocalDateTime signupTime;
}
