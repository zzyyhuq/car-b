package com.example.beauty.common.logging;

import com.example.beauty.common.security.JwtInterceptor;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 访问日志：谁（userId/role）用什么方法请求了哪个接口、状态码、耗时、来源 IP。
 * 只记 /api/**，避免 Swagger 静态资源刷屏；不记录请求体和 Authorization，防止泄露敏感信息。
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class RequestLogFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestLogFilter.class);
    private static final String API_PREFIX = "/api/";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (!request.getRequestURI().startsWith(API_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }
        long start = System.currentTimeMillis();
        try {
            filterChain.doFilter(request, response);
        } finally {
            long cost = System.currentTimeMillis() - start;
            Object userId = request.getAttribute(JwtInterceptor.ATTR_USER_ID);
            Object role = request.getAttribute(JwtInterceptor.ATTR_ROLE);
            String query = request.getQueryString();
            String uri = query == null ? request.getRequestURI() : request.getRequestURI() + "?" + query;
            // 异常处理器会同时设置真实 HTTP 状态码，4xx/5xx 记为 WARN
            if (response.getStatus() >= 400) {
                log.warn("{} {} {} {}ms userId={} role={} ip={}",
                        request.getMethod(), uri, response.getStatus(), cost,
                        userId == null ? "-" : userId, role == null ? "-" : role, clientIp(request));
            } else {
                log.info("{} {} {} {}ms userId={} role={} ip={}",
                        request.getMethod(), uri, response.getStatus(), cost,
                        userId == null ? "-" : userId, role == null ? "-" : role, clientIp(request));
            }
        }
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
