package com.example.beauty.common.security;

/**
 * 当前请求的登录用户。拦截器在 preHandle 里放，afterCompletion 里清，
 * 必须 clear，否则线程池复用线程会串号。
 */
public final class UserContext {

    private static final ThreadLocal<LoginUser> HOLDER = new ThreadLocal<>();

    private UserContext() {
    }

    public static void set(LoginUser user) {
        HOLDER.set(user);
    }

    public static LoginUser get() {
        return HOLDER.get();
    }

    public static Long getId() {
        LoginUser user = HOLDER.get();
        return user == null ? null : user.getId();
    }

    public static String getRole() {
        LoginUser user = HOLDER.get();
        return user == null ? null : user.getRole();
    }

    public static void clear() {
        HOLDER.remove();
    }
}
