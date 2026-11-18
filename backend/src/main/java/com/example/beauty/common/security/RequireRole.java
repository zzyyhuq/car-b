package com.example.beauty.common.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 接口角色校验。标在方法或类上，JwtInterceptor 读取后比对 JWT 里的 role。
 * 用法：@RequireRole({"ADMIN", "STAFF"})
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireRole {

    String[] value();
}
