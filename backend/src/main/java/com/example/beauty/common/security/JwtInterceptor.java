package com.example.beauty.common.security;

import com.example.beauty.common.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;

@Slf4j
public class JwtInterceptor implements HandlerInterceptor {

    /** 请求属性名：供访问日志读取，过滤器执行时 UserContext 已被清理 */
    public static final String ATTR_USER_ID = "loginUserId";
    public static final String ATTR_ROLE = "loginRole";

    private static final String HEADER = "Authorization";
    private static final String PREFIX = "Bearer ";

    private final JwtUtil jwtUtil;

    public JwtInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // CORS 预检请求不带 token，直接放行
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }
        String header = request.getHeader(HEADER);
        if (header == null || !header.startsWith(PREFIX)) {
            log.warn("鉴权失败：未携带 token，{} {}", request.getMethod(), request.getRequestURI());
            throw new BusinessException(401, "请先登录");
        }
        LoginUser user;
        try {
            user = jwtUtil.parse(header.substring(PREFIX.length()));
        } catch (Exception e) {
            log.warn("鉴权失败：token 无效或已过期，{} {}", request.getMethod(), request.getRequestURI());
            throw new BusinessException(401, "登录已过期，请重新登录");
        }
        // 解析成功就记录到请求属性，越权被拒时访问日志也能看出是谁
        request.setAttribute(ATTR_USER_ID, user.getId());
        request.setAttribute(ATTR_ROLE, user.getRole());
        RequireRole requireRole = handlerMethod.getMethodAnnotation(RequireRole.class);
        if (requireRole == null) {
            requireRole = handlerMethod.getBeanType().getAnnotation(RequireRole.class);
        }
        if (requireRole != null && !Arrays.asList(requireRole.value()).contains(user.getRole())) {
            log.warn("越权访问：userId={} role={} 请求 {}", user.getId(), user.getRole(), request.getRequestURI());
            throw new BusinessException(403, "无权访问该接口");
        }
        // 角色校验通过后再放上下文：preHandle 抛异常时 afterCompletion 不会回调，避免 ThreadLocal 泄漏
        UserContext.set(user);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        UserContext.clear();
    }
}
