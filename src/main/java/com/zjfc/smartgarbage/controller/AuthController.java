package com.zjfc.smartgarbage.controller;

import com.zjfc.smartgarbage.model.dto.AuthResponse;
import com.zjfc.smartgarbage.model.dto.LoginRequest;
import com.zjfc.smartgarbage.model.dto.RegisterRequest;
import com.zjfc.smartgarbage.model.vo.ApiResponse;
import com.zjfc.smartgarbage.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {

    private final AuthService authService;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.ok(ApiResponse.success("注册成功", response));
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("登录成功", response));
    }

    /**
     * 刷新Token
     */
    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(@RequestParam String refreshToken) {
        AuthResponse response = authService.refreshToken(refreshToken);
        return ResponseEntity.ok(ApiResponse.success("刷新成功", response));
    }

    /**
     * 修改密码
     */
    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<?>> changePassword(
            @RequestParam String oldPassword,
            @RequestParam String newPassword) {
        authService.changePassword(oldPassword, newPassword);
        return ResponseEntity.ok(ApiResponse.success("密码修改成功", null));
    }

    /**
     * 退出登录
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<?>> logout() {
        authService.logout();
        return ResponseEntity.ok(ApiResponse.success("退出成功", null));
    }
}