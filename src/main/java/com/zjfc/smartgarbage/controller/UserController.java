package com.zjfc.smartgarbage.controller;

import com.zjfc.smartgarbage.model.dto.UpdateUserRequest;
import com.zjfc.smartgarbage.model.vo.ApiResponse;
import com.zjfc.smartgarbage.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Validated
public class UserController {

    private final UserService userService;

    /**
     * 更新用户信息
     */
    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<?>> updateProfile(@Valid @RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(ApiResponse.success("更新成功", userService.updateProfile(request)));
    }

    /**
     * 上传头像
     */
    @PostMapping("/avatar")
    public ResponseEntity<ApiResponse<?>> uploadAvatar(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(ApiResponse.success("上传成功", userService.uploadAvatar(file)));
    }

    /**
     * 获取用户积分详情
     */
    @GetMapping("/points")
    public ResponseEntity<ApiResponse<?>> getUserPoints() {
        return ResponseEntity.ok(ApiResponse.success("获取成功", userService.getUserPointsDetail()));
    }

    /**
     * 获取用户投放记录
     */
    @GetMapping("/delivery-records")
    public ResponseEntity<ApiResponse<?>> getDeliveryRecords(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success("获取成功",
                userService.getDeliveryRecords(page, size)));
    }
}