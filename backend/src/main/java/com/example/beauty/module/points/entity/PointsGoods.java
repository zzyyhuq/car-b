package com.example.beauty.module.points.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 积分商品，对应 points_goods 表。
 */
@Data
@TableName("points_goods")
public class PointsGoods {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String description;

    /** 兑换所需积分 */
    private Integer pointsPrice;

    private Integer stock;

    private String image;

    /** 1 上架，0 下架 */
    private Integer status;

    /** 逻辑删除，已兑换的历史订单不受影响 */
    @JsonIgnore
    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
