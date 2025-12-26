package com.zjfc.smartgarbage.service;

import com.zjfc.smartgarbage.model.dto.UpdateUserRequest;
import com.zjfc.smartgarbage.model.entity.User;
import com.zjfc.smartgarbage.model.vo.UserInfoVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface UserService {

    /**
     * 获取当前登录用户信息
     */
    User getCurrentUser();

    /**
     * 获取当前用户信息（视图对象）
     */
    UserInfoVO getCurrentUserInfo();

    /**
     * 更新用户信息
     */
    UserInfoVO updateProfile(UpdateUserRequest request);

    /**
     * 上传头像
     */
    String uploadAvatar(MultipartFile file);

    /**
     * 获取用户积分详情
     */
    Map<String, Object> getUserPointsDetail();

    /**
     * 获取用户投放记录
     */
    Object getDeliveryRecords(int page, int size);

    /**
     * 根据用户ID获取用户
     */
    User getUserById(String userId);
}