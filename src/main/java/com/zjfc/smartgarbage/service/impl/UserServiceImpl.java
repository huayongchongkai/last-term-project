package com.zjfc.smartgarbage.service.impl;

import com.zjfc.smartgarbage.model.dto.UpdateUserRequest;
import com.zjfc.smartgarbage.model.entity.User;
import com.zjfc.smartgarbage.model.vo.UserInfoVO;
import com.zjfc.smartgarbage.repository.UserRepository;
import com.zjfc.smartgarbage.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public User getCurrentUser() {
        // 这里需要根据JWT token获取当前用户
        // 暂时返回模拟用户用于测试
        return userRepository.findById("USER_TEST").orElse(null);
    }

    @Override
    public UserInfoVO getCurrentUserInfo() {
        User user = getCurrentUser();
        if (user == null) {
            return null;
        }

        return UserInfoVO.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .studentId(user.getStudentId())
                .avatarUrl(user.getAvatarUrl())
                .email(user.getEmail())
                .phone(user.getPhone())
                .dormitory(user.getDormitory())
                .createdAt(user.getCreatedAt())
                .build();
    }

    @Override
    public UserInfoVO updateProfile(UpdateUserRequest request) {
        User user = getCurrentUser();
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 更新字段
        if (request.getUsername() != null) {
            user.setUsername(request.getUsername());
        }
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getDormitory() != null) {
            user.setDormitory(request.getDormitory());
        }

        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        return getCurrentUserInfo();
    }

    @Override
    public String uploadAvatar(MultipartFile file) {
        User user = getCurrentUser();
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 这里应该实现文件上传逻辑
        // 暂时返回模拟URL
        String avatarUrl = "/uploads/avatar/" + System.currentTimeMillis() + ".jpg";
        user.setAvatarUrl(avatarUrl);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        return avatarUrl;
    }

    @Override
    public Map<String, Object> getUserPointsDetail() {
        Map<String, Object> result = new HashMap<>();
        result.put("totalPoints", 100); // 模拟数据
        result.put("availablePoints", 80);
        result.put("level", "普通用户");
        return result;
    }

    @Override
    public Object getDeliveryRecords(int page, int size) {
        // 这里应该查询投放记录
        // 暂时返回空列表
        return new HashMap<>();
    }

    @Override
    public User getUserById(String userId) {
        return userRepository.findById(userId).orElse(null);
    }
}