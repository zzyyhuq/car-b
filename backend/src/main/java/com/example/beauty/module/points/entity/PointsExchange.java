package com.example.beauty.module.points.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 积分兑换单，对应 points_exchange 表。
 */
@Data
@TableName("points_exchange")
public class PointsExchange {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String exchangeNo;

    private Long userId;

    private Long goodsId;

    /** 消耗积分 */
    private Integer pointsCost;

    /** PENDING 待核销 / VERIFIED 已核销 / CANCELLED 已取消 */
    private String status;

    private LocalDateTime exchangeTime;

    /** 到店核销时间 */
    private LocalDateTime verifyTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
