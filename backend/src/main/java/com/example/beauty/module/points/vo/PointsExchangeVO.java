package com.example.beauty.module.points.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 兑换结果 / 兑换记录。兑换接口会额外返回兑换后的积分余额。
 */
@Data
public class PointsExchangeVO {

    private Long id;
    private String exchangeNo;
    private Long goodsId;
    private String goodsName;
    private Integer pointsCost;
    private String status;
    private String statusText;
    private LocalDateTime exchangeTime;
    private LocalDateTime verifyTime;

    /** 兑换后的积分余额，仅兑换接口返回 */
    private Integer pointsBalance;
}
