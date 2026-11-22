package com.example.beauty.common.constant;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 服务过程节点。固定几个常用节点，前端下拉直接选，避免员工随手输入不一致。
 */
public final class ServiceNode {

    public static final String RECEIVE = "RECEIVE";
    public static final String CLEAN = "CLEAN";
    public static final String CHECK = "CHECK";
    public static final String DELIVER = "DELIVER";

    private static final Map<String, String> TEXTS = new LinkedHashMap<>();

    static {
        TEXTS.put(RECEIVE, "接车");
        TEXTS.put(CLEAN, "清洗");
        TEXTS.put(CHECK, "质检");
        TEXTS.put(DELIVER, "交车");
    }

    private ServiceNode() {
    }

    public static boolean valid(String node) {
        return node != null && TEXTS.containsKey(node);
    }

    public static String text(String node) {
        return node == null ? "" : TEXTS.getOrDefault(node, node);
    }
}
