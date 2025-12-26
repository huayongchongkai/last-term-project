package com.zjfc.smartgarbage.config;

import com.zjfc.smartgarbage.filter.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

        private final JwtAuthenticationFilter jwtAuthenticationFilter;

        // 公开访问的URL列表
        private static final String[] PUBLIC_URLS = {
                        // 健康检查和监控端点
                        "/actuator/health",
                        "/actuator/info",

                        // 前端资源
                        "/",
                        "/index.html",
                        "/favicon.ico",
                        "/static/**",
                        "/resources/**",
                        "/css/**", "/js/**", "/images/**",

                        // API文档
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/v3/api-docs/**",
                        "/v3/api-docs",
                        "/webjars/**",
                        "/configuration/ui",
                        "/configuration/security",

                        // 认证接口
                        "/api/auth/**",

                        // 垃圾识别接口 - 确保全部放行
                        "/api/garbage/**",
                        "/api/garbage/recognize",
                        "/api/images/**",
                        "/api/classify/**",

                        // H2数据库控制台（如果使用）
                        "/h2-console/**"
        };

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        public AuthenticationManager authenticationManager(
                        AuthenticationConfiguration authenticationConfiguration) throws Exception {
                return authenticationConfiguration.getAuthenticationManager();
        }

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
                http
                                // 启用CORS
                                .cors().configurationSource(corsConfigurationSource()).and()

                                // 禁用CSRF（因为使用JWT无状态）
                                .csrf().disable()

                                // 无状态会话
                                .sessionManagement()
                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                                .and()

                                // 授权配置
                                .authorizeRequests()
                                // 公开访问的端点
                                .antMatchers(PUBLIC_URLS).permitAll()

                                // 用户相关接口需要认证
                                .antMatchers("/api/users/**").authenticated()

                                // 积分相关接口需要认证
                                .antMatchers("/api/points/**").authenticated()

                                // 管理接口需要管理员角色
                                .antMatchers("/api/admin/**").hasRole("ADMIN")

                                // 其他所有请求都需要认证
                                .anyRequest().authenticated()
                                .and()

                                // 添加JWT过滤器
                                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                                // 异常处理配置
                                .exceptionHandling()
                                .accessDeniedHandler((request, response, accessDeniedException) -> {
                                        response.setStatus(403);
                                        response.setContentType("application/json");
                                        response.getWriter().write(
                                                        "{\"success\": false, \"message\": \"Access Denied: " +
                                                                        accessDeniedException.getMessage() + "\"}");
                                })
                                .authenticationEntryPoint((request, response, authException) -> {
                                        response.setStatus(401);
                                        response.setContentType("application/json");
                                        response.getWriter().write(
                                                        "{\"success\": false, \"message\": \"Unauthorized: " +
                                                                        authException.getMessage() + "\"}");
                                });

                return http.build();
        }

        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration configuration = new CorsConfiguration();

                // 允许的来源
                configuration.setAllowedOrigins(Arrays.asList(
                                "http://localhost:3000", // React/Vue前端
                                "http://localhost:8080", // 当前服务
                                "http://localhost:8081", // 备用端口
                                "http://127.0.0.1:3000",
                                "http://127.0.0.1:8080"));

                // 允许的方法
                configuration.setAllowedMethods(Arrays.asList(
                                "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));

                // 允许的头部
                configuration.setAllowedHeaders(Arrays.asList(
                                "Authorization",
                                "Content-Type",
                                "Accept",
                                "X-Requested-With",
                                "Cache-Control",
                                "Origin",
                                "X-Idempotency-Key" // 你的幂等键
                ));

                // 暴露的头部
                configuration.setExposedHeaders(Arrays.asList(
                                "Authorization",
                                "X-Total-Count",
                                "X-Response-Time"));

                // 允许凭证
                configuration.setAllowCredentials(true);

                // 预检请求缓存时间（秒）
                configuration.setMaxAge(3600L);

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration);
                return source;
        }
}