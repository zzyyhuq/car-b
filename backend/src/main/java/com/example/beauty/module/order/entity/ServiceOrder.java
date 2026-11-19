package com.example.beauty.module.order.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 美容订单。状态流转规则见 {@link com.example.beauty.common.constant.OrderStatus}。
 * 金额在下单时按服务项目价格快照，之后项目调价不影响历史订单。
 */
@Data
@TableName("service_order")
public class ServiceOrder {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String orderNo;

    /** 车主 id */
    private Long userId;

    /** 车辆 id */
    private Long carId;

    /** 服务项目 id */
    private Long serviceTypeId;

    /** 负责员工 id，派单后才有值 */
    private Long staffId;

    private LocalDateTime appointmentTime;

    /** PENDING / ACCEPTED / IN_PROGRESS / COMPLETED / EVALUATED / CANCELLED */
    private String status;

    private BigDecimal amount;

    /** 下单抵扣积分，模块 5 接入后启用 */
    private Integer pointsDeduct;

    private String remark;

    private String cancelReason;

    private LocalDateTime finishTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
