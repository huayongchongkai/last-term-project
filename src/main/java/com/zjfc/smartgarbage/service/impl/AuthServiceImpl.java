package com.zjfc.smartgarbage.service.impl;

import com.zjfc.smartgarbage.model.dto.AuthResponse;
import com.zjfc.smartgarbage.model.dto.LoginRequest;
import com.zjfc.smartgarbage.model.dto.RegisterRequest;
import com.zjfc.smartgarbage.model.entity.User;
import com.zjfc.smartgarbage.model.entity.UserPointsSummary;
import com.zjfc.smartgarbage.repository.UserPointsSummaryRepository;
import com.zjfc.smartgarbage.repository.UserRepository;
import com.zjfc.smartgarbage.service.AuthService;
import com.zjfc.smartgarbage.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest; // 注意：使用 javax.servlet，不是 jakarta.servlet
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final UserPointsSummaryRepository pointsSummaryRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final HttpServletRequest request; // 用于获取当前用户

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("开始注册用户，学号: {}, 用户名: {}", request.getStudentId(), request.getUsername());

        // 检查学号是否已注册
        if (userRepository.existsByStudentId(request.getStudentId())) {
            log.error("学号已存在: {}", request.getStudentId());
            throw new RuntimeException("该学号已注册");
        }

        // 创建用户
        User user = new User();
        user.setUserId(generateUserId());
        user.setStudentId(request.getStudentId());
        user.setUsername(request.getUsername());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setDormitory(request.getDormitory());
        user.setAvatarUrl("/default-avatar.png");
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);
        log.info("用户注册成功: {}, 用户ID: {}", savedUser.getStudentId(), savedUser.getUserId());

        // 初始化用户积分汇总
        UserPointsSummary pointsSummary = new UserPointsSummary();
        pointsSummary.setUserId(savedUser.getUserId());
        pointsSummary.setTotalPoints(0);
        pointsSummary.setAvailablePoints(0);
        pointsSummary.setFrozenPoints(0);
        pointsSummary.setCurrentLevel("普通用户");
        pointsSummary.setCorrectCount(0);
        pointsSummary.setTotalCount(0);
        pointsSummary.setAccuracyRate(0.0);
        pointsSummary.setUpdatedAt(LocalDateTime.now());
        pointsSummaryRepository.save(pointsSummary);
        log.info("初始化用户积分汇总成功: {}", savedUser.getUserId());

        // 生成token
        String accessToken = jwtUtil.generateAccessToken(savedUser);
        String refreshToken = jwtUtil.generateRefreshToken(savedUser);

        return buildAuthResponse(savedUser, accessToken, refreshToken);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        log.info("用户登录尝试，学号: {}", request.getStudentId());

        User user = userRepository.findByStudentId(request.getStudentId())
                .orElseThrow(() -> {
                    log.error("用户不存在: {}", request.getStudentId());
                    return new RuntimeException("用户不存在");
                });

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            log.error("密码错误，学号: {}", request.getStudentId());
            throw new RuntimeException("密码错误");
        }

        // 更新最后登录时间（通过更新时间）
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        // 生成token
        String accessToken = jwtUtil.generateAccessToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user);

        log.info("用户登录成功: {}, 用户ID: {}", user.getStudentId(), user.getUserId());
        return buildAuthResponse(user, accessToken, refreshToken);
    }

    @Override
    public AuthResponse refreshToken(String refreshToken) {
        log.info("刷新Token请求");

        if (!jwtUtil.validateToken(refreshToken)) {
            log.error("Refresh token无效");
            throw new RuntimeException("Refresh token无效");
        }

        String userId = jwtUtil.getUserIdFromToken(refreshToken);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("用户不存在，用户ID: {}", userId);
                    return new RuntimeException("用户不存在");
                });

        String newAccessToken = jwtUtil.generateAccessToken(user);
        String newRefreshToken = jwtUtil.generateRefreshToken(user);

        log.info("Token刷新成功，用户ID: {}", userId);
        return buildAuthResponse(user, newAccessToken, newRefreshToken);
    }

    @Override
    @Transactional
    public void changePassword(String oldPassword, String newPassword) {
        User currentUser = getCurrentUser();

        if (currentUser == null) {
            throw new RuntimeException("用户未登录");
        }

        if (!passwordEncoder.matches(oldPassword, currentUser.getPasswordHash())) {
            throw new RuntimeException("原密码错误");
        }

        currentUser.setPasswordHash(passwordEncoder.encode(newPassword));
        currentUser.setUpdatedAt(LocalDateTime.now());
        userRepository.save(currentUser);

        log.info("用户修改密码成功: {}", currentUser.getStudentId());
    }

    @Override
    public void logout() {
        // 在实际项目中，这里可以将token加入黑名单
        // 目前只是记录日志
        log.info("用户退出登录");
    }

    private AuthResponse buildAuthResponse(User user, String accessToken, String refreshToken) {
        // 获取用户积分信息
        UserPointsSummary pointsSummary = pointsSummaryRepository.findById(user.getUserId())
                .orElseGet(() -> {
                    log.warn("用户积分记录不存在，创建默认记录，用户ID: {}", user.getUserId());

                    UserPointsSummary summary = new UserPointsSummary();
                    summary.setUserId(user.getUserId());
                    summary.setTotalPoints(0);
                    summary.setAvailablePoints(0);
                    summary.setFrozenPoints(0);
                    summary.setCurrentLevel("普通用户");
                    summary.setCorrectCount(0);
                    summary.setTotalCount(0);
                    summary.setAccuracyRate(0.0);
                    summary.setUpdatedAt(LocalDateTime.now());

                    pointsSummaryRepository.save(summary);
                    return summary;
                });

        // 计算等级（基于积分）
        int level = calculateLevel(pointsSummary.getTotalPoints());
        String levelName = getLevelName(level);

        log.info("构建认证响应，用户ID: {}, 等级: {}, 积分: {}",
                user.getUserId(), level, pointsSummary.getTotalPoints());

        return AuthResponse.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .studentId(user.getStudentId())
                .avatarUrl(user.getAvatarUrl())
                .totalPoints(pointsSummary.getTotalPoints())
                .availablePoints(pointsSummary.getAvailablePoints())
                .frozenPoints(pointsSummary.getFrozenPoints())
                .level(level)
                .levelName(levelName)
                .correctCount(pointsSummary.getCorrectCount())
                .totalCount(pointsSummary.getTotalCount())
                .accuracyRate(pointsSummary.getAccuracyRate())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(3600L) // 1小时
                .build();
    }

    private int calculateLevel(int totalPoints) {
        if (totalPoints >= 5000)
            return 5;
        else if (totalPoints >= 1000)
            return 4;
        else if (totalPoints >= 500)
            return 3;
        else if (totalPoints >= 100)
            return 2;
        else
            return 1;
    }

    private String getLevelName(int level) {
        switch (level) {
            case 1:
                return "普通用户";
            case 2:
                return "白银用户";
            case 3:
                return "黄金用户";
            case 4:
                return "铂金用户";
            case 5:
                return "钻石用户";
            default:
                return "普通用户";
        }
    }

    private String generateUserId() {
        return "USER_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private User getCurrentUser() {
        try {
            // 从 SecurityContext 获取当前认证用户
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                String username = authentication.getName();
                log.debug("从SecurityContext获取用户名: {}", username);

                // 尝试用用户名查找
                return userRepository.findByUsername(username)
                        .orElseGet(() -> {
                            // 如果用户名找不到，尝试用studentId查找
                            return userRepository.findByStudentId(username)
                                    .orElseThrow(() -> new RuntimeException("用户不存在"));
                        });
            }
        } catch (Exception e) {
            log.error("获取当前用户失败: {}", e.getMessage());
        }

        // 如果上述方法失败，尝试从JWT token获取
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                String token = authHeader.substring(7);
                String userId = jwtUtil.getUserIdFromToken(token);
                return userRepository.findById(userId)
                        .orElseThrow(() -> new RuntimeException("用户不存在"));
            } catch (Exception e) {
                log.error("从JWT token获取用户失败: {}", e.getMessage());
            }
        }

        return null;
    }
}