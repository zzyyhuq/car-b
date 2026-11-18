package com.example.beauty.common.constant;

/**
 * 三类角色。故意用常量而不是 RBAC 表：毕设体量够用，答辩也好讲。
 */
public final class Role {

    public static final String ADMIN = "ADMIN";
    public static final String STAFF = "STAFF";
    public static final String OWNER = "OWNER";

    private Role() {
    }
}
