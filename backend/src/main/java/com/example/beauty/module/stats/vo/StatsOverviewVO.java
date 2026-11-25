package com.example.beauty.module.stats.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 管理端数据概览。
 */
@Data
public class StatsOverviewVO {

    private long userCount;
    private long ownerCount;
    private long staffCount;

    private long orderCount;
    private long todayOrderCount;
    private long pendingOrderCount;
    private long completedOrderCount;
    private BigDecimal totalRevenue;

    private long pendingExchangeCount;
    private long pendingMessageCount;
    private long noticeCount;
    private long activityCount;
    private long signupCount;
}
