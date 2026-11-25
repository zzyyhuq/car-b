package com.example.beauty.module.activitysignup.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 活动报名，对应 activity_signup 表。
 * 只有 need_signup=1 的活动才允许报名；取消是把 status 改成 0，不删记录。
 */
@Data
@TableName("activity_signup")
public class ActivitySignup {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long activityId;

    private Long userId;

    private LocalDateTime signupTime;

    /** 1 已报名，0 已取消 */
    private Integer status;
}
