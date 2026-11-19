package com.example.beauty.module.servicetype.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 美容订单类型，也就是车主端看到的「服务项目」。
 */
@Data
@TableName("service_type")
public class ServiceType {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    /** 洗车 / 内饰 / 漆面 / 保养 */
    private String category;

    private String description;

    private BigDecimal price;

    /** 预计时长（分钟） */
    private Integer duration;

    private String image;

    /** 积分倍率，订单完成按 实付金额 × 倍率 发积分 */
    private BigDecimal pointsRate;

    /** 1 上架，0 下架 */
    private Integer status;

    /** 逻辑删除标记，@TableLogic 让删除自动变成 UPDATE */
    @JsonIgnore
    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
