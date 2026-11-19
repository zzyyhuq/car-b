package com.example.beauty.common.constant;

import java.util.Map;
import java.util.Set;

/**
 * 订单状态机：待接单 → 已接单 → 服务中 → 已完成 → 已评价。
 * 仅待接单/已接单可以取消。
 * 状态流转规则集中在这里，Controller 和 Service 都只调用本类判断，避免散落。
 */
public final class OrderStatus {

    public static final String PENDING = "PENDING";
    public static final String ACCEPTED = "ACCEPTED";
    public static final String IN_PROGRESS = "IN_PROGRESS";
    public static final String COMPLETED = "COMPLETED";
    public static final String EVALUATED = "EVALUATED";
    public static final String CANCELLED = "CANCELLED";

    /** 当前状态 → 允许流转到的状态 */
    private static final Map<String, Set<String>> TRANSITIONS = Map.of(
            PENDING, Set.of(ACCEPTED, CANCELLED),
            ACCEPTED, Set.of(IN_PROGRESS, CANCELLED),
            IN_PROGRESS, Set.of(COMPLETED),
            COMPLETED, Set.of(EVALUATED),
            EVALUATED, Set.of(),
            CANCELLED, Set.of()
    );

    private OrderStatus() {
    }

    public static boolean valid(String status) {
        return status != null && TRANSITIONS.containsKey(status);
    }

    public static boolean canTransit(String from, String to) {
        Set<String> next = TRANSITIONS.get(from);
        return next != null && next.contains(to);
    }

    /** 只有待接单、已接单可以取消 */
    public static boolean cancellable(String status) {
        return PENDING.equals(status) || ACCEPTED.equals(status);
    }

    public static String text(String status) {
        if (status == null) {
            return "";
        }
        return switch (status) {
            case PENDING -> "待接单";
            case ACCEPTED -> "已接单";
            case IN_PROGRESS -> "服务中";
            case COMPLETED -> "已完成";
            case EVALUATED -> "已评价";
            case CANCELLED -> "已取消";
            default -> status;
        };
    }
}
