package com.example.beauty.common.exception;

import com.example.beauty.common.Result;
import com.example.beauty.common.security.JwtInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * 统一异常处理：响应体保持 { code, message, data } 不变，
 * 同时把 HTTP 状态码也设置成对应值，便于访问日志、网关和监控识别失败请求。
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusiness(BusinessException e, HttpServletResponse response) {
        setStatus(response, e.getCode());
        return Result.fail(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidation(MethodArgumentNotValidException e, HttpServletResponse response) {
        FieldError error = e.getBindingResult().getFieldError();
        String message = error == null ? "参数不合法" : error.getDefaultMessage();
        response.setStatus(400);
        return Result.fail(400, message);
    }

    /** 接口不存在（路径写错，或功能还没实现） */
    @ExceptionHandler(NoResourceFoundException.class)
    public Result<Void> handleNotFound(NoResourceFoundException e, HttpServletRequest request,
                                       HttpServletResponse response) {
        log.warn("接口不存在：{} {}，资源 /{}", request.getMethod(), request.getRequestURI(), e.getResourcePath());
        response.setStatus(404);
        return Result.fail(404, "接口不存在：" + e.getResourcePath());
    }

    /** 请求方法用错，比如用 GET 调 POST 登录 */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public Result<Void> handleMethodNotSupported(HttpRequestMethodNotSupportedException e,
                                                 HttpServletResponse response) {
        response.setStatus(405);
        return Result.fail(405, "请求方法不支持，请使用 " + e.getSupportedHttpMethods());
    }

    /** 请求体不是合法 JSON、字段类型不对 */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<Void> handleNotReadable(HttpMessageNotReadableException e, HttpServletResponse response) {
        response.setStatus(400);
        return Result.fail(400, "请求体格式错误，请检查 JSON 是否正确");
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Result<Void> handleMissingParam(MissingServletRequestParameterException e,
                                           HttpServletResponse response) {
        response.setStatus(400);
        return Result.fail(400, "缺少参数：" + e.getParameterName());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public Result<Void> handleTypeMismatch(MethodArgumentTypeMismatchException e, HttpServletResponse response) {
        response.setStatus(400);
        return Result.fail(400, "参数类型错误：" + e.getName());
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleOther(Exception e, HttpServletRequest request, HttpServletResponse response) {
        log.error("系统异常：{} {} userId={}", request.getMethod(), request.getRequestURI(),
                request.getAttribute(JwtInterceptor.ATTR_USER_ID), e);
        response.setStatus(500);
        return Result.fail(500, "系统繁忙，请稍后再试");
    }

    /** 业务 code 正常就是 HTTP 状态码；万一不是合法值，按 500 处理 */
    private void setStatus(HttpServletResponse response, int code) {
        response.setStatus(code >= 100 && code <= 599 ? code : 500);
    }
}
