package com.example.beauty.module.points.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 积分流水，对应 points_record 表。
 * changeAmount 为正表示获得，为负表示消耗。
 */
@Data
@TableName("points_record")
public class PointsRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Integer changeAmount;

    /** ORDER_GAIN 订单获得 / EXCHANGE_COST 兑换消耗 / ADMIN_ADJUST 管理员调整 */
    private String type;

    /** 关联订单或兑换单 id */
    private Long refId;

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
