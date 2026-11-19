package com.example.beauty.module.order.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单展示对象：把服务项目名、车牌、车主/员工昵称一起带出来，
 * 前端列表不用再逐个查字典。
 */
@Data
public class OrderVO {

    private Long id;
    private String orderNo;

    private Long userId;
    private String userName;

    private Long carId;
    private String plateNo;
    private String carBrand;
    private String carModel;

    private Long serviceTypeId;
    private String serviceName;

    private Long staffId;
    private String staffName;

    private LocalDateTime appointmentTime;

    private String status;
    private String statusText;

    private BigDecimal amount;
    private String remark;
    private String cancelReason;
    private LocalDateTime finishTime;
    private LocalDateTime createTime;
}
