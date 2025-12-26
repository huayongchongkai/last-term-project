package com.zjfc.smartgarbage.filter;

import com.zjfc.smartgarbage.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    // 不需要JWT验证的公开接口列表（必须与SecurityConfig保持一致）
    private static final List<String> PUBLIC_ENDPOINTS = Arrays.asList(
            // Actuator健康检查
            "/actuator/health",
            "/actuator/info",

            // 垃圾识别相关（最重要的！）
            "/api/garbage/",
            "/api/images/",
            "/api/classify/",

            // 用户认证
            "/api/auth/",

            // API文档
            "/swagger-ui",
            "/v3/api-docs",
            "/webjars/",

            // 静态资源
            "/static/",
            "/css/", "/js/", "/images/",

            // 根路径
            "/",
            "/index.html",
            "/favicon.ico");

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String requestURI = request.getRequestURI();

        // 1. 如果是公开接口，直接跳过JWT验证
        if (isPublicEndpoint(requestURI)) {
            logger.debug("跳过JWT验证（公开接口）: " + requestURI);
            filterChain.doFilter(request, response);
            return;
        }

        // 2. 从Header获取JWT
        final String authHeader = request.getHeader("Authorization");
        String jwt = null;
        String username = null;

        // 3. 验证Authorization头格式
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            jwt = authHeader.substring(7);
            try {
                username = jwtUtil.getUserIdFromToken(jwt);
                logger.debug("从JWT解析用户: " + username);
            } catch (Exception e) {
                logger.error("JWT token解析失败: " + e.getMessage());
                // 继续执行，不设置认证信息
            }
        } else {
            logger.debug("请求缺少或格式错误的Authorization头: " + requestURI);
        }

        // 4. 设置认证信息到SecurityContext
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            if (jwtUtil.validateToken(jwt)) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(username, null,
                        null);
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
                logger.debug("用户认证成功: " + username);
            } else {
                logger.warn("JWT验证失败: " + jwt);
            }
        }

        // 5. 继续过滤器链
        filterChain.doFilter(request, response);
    }

    /**
     * 检查是否为公开接口
     */
    private boolean isPublicEndpoint(String requestURI) {
        for (String endpoint : PUBLIC_ENDPOINTS) {
            if (requestURI.startsWith(endpoint)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 跳过OPTIONS请求（预检请求）的过滤
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String method = request.getMethod();
        String uri = request.getRequestURI();

        // OPTIONS请求直接跳过（CORS预检）
        if ("OPTIONS".equalsIgnoreCase(method)) {
            logger.debug("跳过OPTIONS请求: " + uri);
            return true;
        }

        return false;
    }
}