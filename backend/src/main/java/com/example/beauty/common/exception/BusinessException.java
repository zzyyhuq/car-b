package com.example.beauty.common.exception;

import lombok.Getter;

/**
 * 业务异常。code 沿用 HTTP 语义：
 * 400 参数/业务错误，401 未登录，403 无权限，404 不存在，500 系统错误。
 */
@Getter
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(String message) {
        this(400, message);
    }
}
