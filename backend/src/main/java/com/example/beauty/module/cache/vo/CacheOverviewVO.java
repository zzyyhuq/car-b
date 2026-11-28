package com.example.beauty.module.cache.vo;

import lombok.Data;

/**
 * 管理端缓存概览：连接状态 + Redis 运行信息。
 */
@Data
public class CacheOverviewVO {

    /** Redis 是否可用；不可用时业务自动降级为直接查库 */
    private boolean connected;

    private String version;
    private String usedMemory;
    private long keyCount;
    /** 0 ~ 1，Redis 累计命中率 */
    private double hitRate;
    private long uptimeSeconds;

    /** 不可用时的提示文案 */
    private String message;
}
