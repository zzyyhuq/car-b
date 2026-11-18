package com.example.beauty.module.user.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 三种角色共用一张表，用 role 字段区分。
 * 车主用 openid 登录（password 为空），管理员/员工用 username + password。
 */
@Data
@TableName("sys_user")
public class SysUser {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 微信登录唯一标识，管理员/员工为空 */
    private String openid;

    /** 管理端账号，车主为空 */
    private String username;

    /** BCrypt 哈希，绝不能返回给前端 */
    @JsonIgnore
    private String password;

    private String nickname;

    private String avatar;

    private String phone;

    /** ADMIN / STAFF / OWNER */
    private String role;

    /** 积分余额 */
    private Integer points;

    /** 1 正常，0 禁用 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
