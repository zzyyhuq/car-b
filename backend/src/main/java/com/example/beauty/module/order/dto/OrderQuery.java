package com.example.beauty.module.order.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * 订单分页查询，车主端和管理端共用。车主端在 Service 里强制按当前用户过滤。
 */
@Data
public class OrderQuery {

    private long page = 1;
    private long size = 10;

    /** 订单号模糊搜索 */
    private String orderNo;

    private String status;

    /** 车牌号模糊搜索，管理端用 */
    private String plateNo;

    private Long staffId;

    /** 下单日期范围（含起止两天） */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate endDate;
}
