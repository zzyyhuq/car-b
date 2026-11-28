package com.example.beauty.common.cache;

import java.time.Duration;

/**
 * 业务缓存 key 与过期时间集中定义。
 * 统一 beauty: 前缀，管理端只能查看/删除这个前缀下的 key，避免误删其它应用的数据。
 */
public final class CacheKeys {

    /** 所有业务缓存的统一前缀 */
    public static final String PREFIX = "beauty:";

    public static final String SERVICE_TYPE_LIST = PREFIX + "service-type:list";
    public static final String NOTICE_LIST = PREFIX + "notice:list";
    public static final String ACTIVITY_LIST = PREFIX + "activity:list";

    /** 服务项目变动少，缓存久一点 */
    public static final Duration SERVICE_TYPE_TTL = Duration.ofMinutes(30);
    /** 公告/活动可能随时发布或下线，缓存短一点 */
    public static final Duration NOTICE_TTL = Duration.ofMinutes(5);
    public static final Duration ACTIVITY_TTL = Duration.ofMinutes(5);

    public static String noticeList(int limit) {
        return NOTICE_LIST + ":" + limit;
    }

    public static String activityList(int limit) {
        return ACTIVITY_LIST + ":" + limit;
    }

    private CacheKeys() {
    }
}
