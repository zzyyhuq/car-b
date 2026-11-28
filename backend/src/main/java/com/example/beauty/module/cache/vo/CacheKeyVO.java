package com.example.beauty.module.cache.vo;

import lombok.Data;

/**
 * 管理端缓存条目。
 */
@Data
public class CacheKeyVO {

    private String key;
    /** Redis 数据类型：string / hash / list ... */
    private String type;
    /** 剩余秒数：-1 永不过期，-2 已过期/不存在 */
    private long ttl;
    /** 业务含义说明 */
    private String description;
}
