package com.zjfc.smartgarbage.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoVO {
    private String userId;
    private String username;
    private String studentId;
    private String avatarUrl;
    private String email;
    private String phone;
    private String dormitory;
    private LocalDateTime createdAt;
}