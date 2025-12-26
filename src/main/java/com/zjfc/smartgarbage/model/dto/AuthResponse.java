package com.zjfc.smartgarbage.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String userId;
    private String username;
    private String studentId;
    private String avatarUrl;

    // 积分相关
    private Integer totalPoints;
    private Integer availablePoints;
    private Integer frozenPoints;

    // 等级相关
    private Integer level;
    private String levelName;

    // 统计信息
    private Integer correctCount;
    private Integer totalCount;
    private Double accuracyRate;

    // Token相关
    private String accessToken;
    private String refreshToken;
    private Long expiresIn;
}