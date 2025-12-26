package com.zjfc.smartgarbage.service;

import com.zjfc.smartgarbage.model.dto.AuthResponse;
import com.zjfc.smartgarbage.model.dto.LoginRequest;
import com.zjfc.smartgarbage.model.dto.RegisterRequest;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse refreshToken(String refreshToken);

    void changePassword(String oldPassword, String newPassword);

    void logout();
}