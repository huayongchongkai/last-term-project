package com.zjfc.smartgarbage.controller;

import com.zjfc.smartgarbage.model.dto.ExchangeRequest;
import com.zjfc.smartgarbage.model.vo.ApiResponse;
import com.zjfc.smartgarbage.service.PointsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/points")
@RequiredArgsConstructor
@Validated
public class PointsController {

    private final PointsService pointsService;

    /**
     * 获取可兑换物品列表
     */
    @GetMapping("/exchange-items")
    public ResponseEntity<ApiResponse<?>> getExchangeItems() {
        try {
            Object items = pointsService.getExchangeItems();
            return ResponseEntity.ok(ApiResponse.success("获取成功", items));
        } catch (Exception e) {
            log.error("获取兑换物品失败", e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("获取失败: " + e.getMessage()));
        }
    }

    /**
     * 兑换物品
     */
    @PostMapping("/exchange")
    public ResponseEntity<ApiResponse<?>> exchangeItem(@Valid @RequestBody ExchangeRequest request) {
        try {
            Object result = pointsService.exchangeItem(request);
            return ResponseEntity.ok(ApiResponse.success("兑换成功", result));
        } catch (Exception e) {
            log.error("兑换物品失败", e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("兑换失败: " + e.getMessage()));
        }
    }

    /**
     * 获取兑换记录
     */
    @GetMapping("/exchange-records")
    public ResponseEntity<ApiResponse<?>> getExchangeRecords(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Object records = pointsService.getExchangeRecords(page, size);
            return ResponseEntity.ok(ApiResponse.success("获取成功", records));
        } catch (Exception e) {
            log.error("获取兑换记录失败", e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("获取失败: " + e.getMessage()));
        }
    }

    /**
     * 获取积分排行榜
     */
    @GetMapping("/leaderboard")
    public ResponseEntity<ApiResponse<?>> getLeaderboard(
            @RequestParam(defaultValue = "10") int top) {
        try {
            Object leaderboard = pointsService.getPointsLeaderboard(top);
            return ResponseEntity.ok(ApiResponse.success("获取成功", leaderboard));
        } catch (Exception e) {
            log.error("获取排行榜失败", e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("获取失败: " + e.getMessage()));
        }
    }

    /**
     * 手动添加积分（测试用）
     */
    @PostMapping("/add")
    public ResponseEntity<ApiResponse<?>> addPoints(@RequestBody Map<String, Object> params) {
        try {
            String type = (String) params.get("type");
            Integer points = (Integer) params.get("points");
            String remark = (String) params.get("remark");

            if (type == null || points == null) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("参数错误：type和points不能为空"));
            }

            pointsService.addPoints(type, points, remark);
            return ResponseEntity.ok(ApiResponse.success("积分添加成功"));

        } catch (Exception e) {
            log.error("添加积分失败", e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("添加积分失败: " + e.getMessage()));
        }
    }
}