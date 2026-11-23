package com.example.beauty.module.points.dto;

import lombok.Data;

@Data
public class PointsExchangeQuery {

    private long page = 1;
    private long size = 10;

    /** 兑换单号模糊搜索 */
    private String exchangeNo;

    /** PENDING / VERIFIED / CANCELLED */
    private String status;

    private Long userId;

    private Long goodsId;
}
