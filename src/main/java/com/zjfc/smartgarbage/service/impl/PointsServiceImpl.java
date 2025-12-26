package com.zjfc.smartgarbage.service.impl;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.zjfc.smartgarbage.config.PointsConfig;
import com.zjfc.smartgarbage.model.dto.ExchangeRequest;
import com.zjfc.smartgarbage.model.entity.PointsExchange;
import com.zjfc.smartgarbage.model.entity.PointsRecord;
import com.zjfc.smartgarbage.model.entity.UserPointsSummary;
import com.zjfc.smartgarbage.model.vo.ExchangeItemVO;
import com.zjfc.smartgarbage.model.vo.ExchangeRecordVO;
import com.zjfc.smartgarbage.model.vo.LeaderboardVO;
import com.zjfc.smartgarbage.repository.PointsExchangeRepository;
import com.zjfc.smartgarbage.repository.PointsRecordRepository;
import com.zjfc.smartgarbage.repository.UserPointsSummaryRepository;
import com.zjfc.smartgarbage.service.PointsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class PointsServiceImpl implements PointsService {

    private final PointsConfig pointsConfig;
    private final UserPointsSummaryRepository pointsSummaryRepository;
    private final PointsRecordRepository pointsRecordRepository;
    private final PointsExchangeRepository pointsExchangeRepository;

    // 模拟兑换物品数据
    private final Map<String, ExchangeItemVO> exchangeItems = createExchangeItems();

    private Map<String, ExchangeItemVO> createExchangeItems() {
        Map<String, ExchangeItemVO> items = new HashMap<>();

        items.put("ITEM_001", ExchangeItemVO.builder()
                .itemId("ITEM_001")
                .name("食堂5元代金券")
                .description("可在学校食堂使用")
                .pointsRequired(50)
                .stock(100)
                .imageUrl("/exchange/coupon1.jpg")
                .category("COUPON")
                .build());

        items.put("ITEM_002", ExchangeItemVO.builder()
                .itemId("ITEM_002")
                .name("校园咖啡8折券")
                .description("校园咖啡厅专用")
                .pointsRequired(80)
                .stock(50)
                .imageUrl("/exchange/coupon2.jpg")
                .category("COUPON")
                .build());

        items.put("ITEM_003", ExchangeItemVO.builder()
                .itemId("ITEM_003")
                .name("环保购物袋")
                .description("可重复使用环保袋")
                .pointsRequired(150)
                .stock(30)
                .imageUrl("/exchange/bag.jpg")
                .category("GIFT")
                .build());

        return items;
    }

    @Override
    @Transactional
    public void addPoints(String type, Integer points, String remark) {
        String userId = getCurrentUserId();
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }

        // 获取或创建用户积分汇总
        UserPointsSummary summary = pointsSummaryRepository.findById(userId)
                .orElseGet(() -> createDefaultSummary(userId));

        // 检查今日积分上限
        if (exceedsDailyLimit(userId, points)) {
            throw new RuntimeException("今日积分已达上限");
        }

        // 更新积分
        summary.setTotalPoints(summary.getTotalPoints() + points);
        summary.setAvailablePoints(summary.getAvailablePoints() + points);
        summary.setUpdatedAt(LocalDateTime.now());

        // 如果是正确投放，更新统计
        if ("DELIVERY".equals(type) || "CORRECTION".equals(type)) {
            summary.setCorrectCount(summary.getCorrectCount() + 1);
            summary.setTotalCount(summary.getTotalCount() + 1);
            if (summary.getTotalCount() > 0) {
                double accuracy = (double) summary.getCorrectCount() / summary.getTotalCount() * 100;
                summary.setAccuracyRate(Math.round(accuracy * 100.0) / 100.0);
            }
            summary.setLastDeliveryTime(LocalDateTime.now());
        }

        // 更新等级
        updateUserLevel(summary);
        pointsSummaryRepository.save(summary);

        // 记录积分流水
        PointsRecord record = new PointsRecord();
        record.setUserId(userId);
        record.setPointsChange(points);
        record.setChangeType(type);
        record.setChangeReason(getChangeReasonByType(type));
        record.setRemark(remark);
        record.setCreatedAt(LocalDateTime.now());

        pointsRecordRepository.save(record);

        log.info("用户 {} 获得 {} 积分，类型: {}", userId, points, type);
    }

    @Override
    @Transactional
    public void deductPoints(Integer points, String remark) {
        String userId = getCurrentUserId();
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }

        UserPointsSummary summary = pointsSummaryRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户积分汇总不存在"));

        if (summary.getAvailablePoints() < points) {
            throw new RuntimeException("积分不足");
        }

        summary.setAvailablePoints(summary.getAvailablePoints() - points);
        summary.setFrozenPoints(summary.getFrozenPoints() + points);
        summary.setUpdatedAt(LocalDateTime.now());
        pointsSummaryRepository.save(summary);

        // 记录扣分流水
        PointsRecord record = new PointsRecord();
        record.setUserId(userId);
        record.setPointsChange(-points);
        record.setChangeType("EXCHANGE");
        record.setChangeReason("积分兑换");
        record.setRemark(remark);
        record.setCreatedAt(LocalDateTime.now());
        pointsRecordRepository.save(record);
    }

    @Override
    @Transactional
    public Object exchangeItem(ExchangeRequest request) {
        try {
            ExchangeItemVO item = exchangeItems.get(request.getItemId());
            if (item == null) {
                throw new RuntimeException("兑换物品不存在");
            }

            if (item.getStock() <= 0) {
                throw new RuntimeException("物品库存不足");
            }

            int totalPoints = item.getPointsRequired() * request.getQuantity();

            // 检查积分是否充足
            String userId = getCurrentUserId();
            UserPointsSummary summary = pointsSummaryRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("用户积分汇总不存在"));

            if (summary.getAvailablePoints() < totalPoints) {
                throw new RuntimeException("积分不足");
            }

            // 扣减积分
            deductPoints(totalPoints, "兑换:" + item.getName() + "x" + request.getQuantity());

            // 减少库存
            item.setStock(item.getStock() - request.getQuantity());
            exchangeItems.put(request.getItemId(), item);

            // 记录兑换记录
            PointsExchange exchange = new PointsExchange();
            exchange.setUserId(userId);
            exchange.setItemName(item.getName());
            exchange.setItemType(getItemTypeByCategory(item.getCategory()));
            exchange.setPointsCost(totalPoints);
            exchange.setExchangeStatus("PENDING");

            // 组装配送信息
            String deliveryInfo = buildDeliveryInfo(request);
            exchange.setDeliveryInfo(deliveryInfo);

            exchange.setExchangeTime(LocalDateTime.now());
            exchange.setRemark(request.getAddress() != null ? "配送至: " + request.getAddress() : "电子券兑换");

            pointsExchangeRepository.save(exchange);

            // 返回前端需要的数据
            return ExchangeRecordVO.builder()
                    .exchangeId(exchange.getExchangeId().toString())
                    .itemName(item.getName())
                    .quantity(request.getQuantity())
                    .pointsUsed(totalPoints)
                    .status("等待处理")
                    .exchangeTime(LocalDateTime.now())
                    .remark(exchange.getRemark())
                    .build();

        } catch (Exception e) {
            log.error("兑换物品失败", e);
            throw new RuntimeException("兑换失败: " + e.getMessage());
        }
    }

    @Override
    public Object getExchangeItems() {
        try {
            List<ExchangeItemVO> result = new ArrayList<>();
            for (ExchangeItemVO item : exchangeItems.values()) {
                if (item.getStock() > 0) {
                    result.add(item);
                }
            }

            // 返回结构化的结果
            Map<String, Object> response = new HashMap<>();
            response.put("total", result.size());
            response.put("items", result);
            return response;

        } catch (Exception e) {
            log.error("获取兑换物品失败", e);
            return Collections.emptyList();
        }
    }

    @Override
    public Object getExchangeRecords(int page, int size) {
        String userId = getCurrentUserId();
        if (userId == null) {
            return Collections.emptyList();
        }

        try {
            Page<PointsExchange> exchanges = pointsExchangeRepository.findByUserId(
                    userId, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));

            return exchanges.map(exchange -> ExchangeRecordVO.builder()
                    .exchangeId(exchange.getExchangeId().toString())
                    .itemName(exchange.getItemName())
                    .quantity(1)
                    .pointsUsed(exchange.getPointsCost())
                    // 关键修正：使用正确的字段名
                    .status(getStatusText(exchange.getExchangeStatus()))
                    .exchangeTime(exchange.getExchangeTime())
                    .build());
        } catch (Exception e) {
            log.error("查询兑换记录失败", e);
            return Collections.emptyList();
        }
    }

    @Override
    public Object getPointsLeaderboard(int top) {
        try {
            List<UserPointsSummary> summaries = pointsSummaryRepository.findAll(
                    PageRequest.of(0, top, Sort.by(Sort.Direction.DESC, "totalPoints")))
                    .getContent();

            List<LeaderboardVO> leaderboard = new ArrayList<>();
            int rank = 1;

            for (UserPointsSummary summary : summaries) {
                leaderboard.add(LeaderboardVO.builder()
                        .rank(rank++)
                        .userId(summary.getUserId())
                        .totalPoints(summary.getTotalPoints())
                        .availablePoints(summary.getAvailablePoints())
                        .levelName(summary.getCurrentLevel())
                        .accuracyRate(summary.getAccuracyRate())
                        .build());
            }

            return leaderboard;
        } catch (Exception e) {
            log.error("查询排行榜失败", e);
            return Collections.emptyList();
        }
    }

    // ============= 辅助方法 =============

    private UserPointsSummary createDefaultSummary(String userId) {
        UserPointsSummary summary = new UserPointsSummary();
        summary.setUserId(userId);
        summary.setTotalPoints(0);
        summary.setAvailablePoints(0);
        summary.setFrozenPoints(0);
        summary.setCurrentLevel("普通用户");
        summary.setCorrectCount(0);
        summary.setTotalCount(0);
        summary.setAccuracyRate(0.0);
        return summary;
    }

    private boolean exceedsDailyLimit(String userId, Integer pointsToAdd) {
        try {
            LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
            LocalDateTime tomorrowStart = todayStart.plusDays(1);

            List<PointsRecord> todayRecords = pointsRecordRepository.findByUserIdAndCreatedAtBetween(
                    userId, todayStart, tomorrowStart);

            int todayTotal = 0;
            for (PointsRecord record : todayRecords) {
                if (record.getPointsChange() > 0) {
                    todayTotal += record.getPointsChange();
                }
            }

            Integer dailyMax = pointsConfig.getDailyMax() != null ? pointsConfig.getDailyMax() : 100;

            return todayTotal + pointsToAdd > dailyMax;
        } catch (Exception e) {
            log.error("检查每日上限失败", e);
            return false;
        }
    }

    private void updateUserLevel(UserPointsSummary summary) {
        int totalPoints = summary.getTotalPoints();
        String levelName;

        if (totalPoints >= 5000) {
            levelName = "钻石用户";
        } else if (totalPoints >= 1000) {
            levelName = "铂金用户";
        } else if (totalPoints >= 500) {
            levelName = "黄金用户";
        } else if (totalPoints >= 100) {
            levelName = "白银用户";
        } else {
            levelName = "普通用户";
        }

        summary.setCurrentLevel(levelName);
    }

    private String getStatusText(String status) {
        if (status == null)
            return "未知状态";

        switch (status) {
            case "PENDING":
                return "等待处理";
            case "PROCESSING":
                return "处理中";
            case "COMPLETED":
                return "已完成";
            case "CANCELLED":
                return "已取消";
            default:
                return "未知状态";
        }
    }

    private String getChangeReasonByType(String type) {
        switch (type) {
            case "DELIVERY":
                return "垃圾投放";
            case "SHARE":
                return "分享";
            case "CORRECTION":
                return "纠错";
            case "EXCHANGE":
                return "积分兑换";
            case "FEEDBACK":
                return "反馈建议";
            default:
                return "其他";
        }
    }

    private String getItemTypeByCategory(String category) {
        switch (category) {
            case "COUPON":
                return "COUPON";
            case "GIFT":
                return "GIFT";
            default:
                return "PRIVILEGE";
        }
    }

    private String buildDeliveryInfo(ExchangeRequest request) {
        StringBuilder info = new StringBuilder();
        if (request.getAddress() != null && !request.getAddress().isEmpty()) {
            info.append("地址: ").append(request.getAddress()).append("\n");
        }
        if (request.getContactPhone() != null && !request.getContactPhone().isEmpty()) {
            info.append("联系电话: ").append(request.getContactPhone());
        }
        return info.toString();
    }

    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("用户未登录或认证信息无效");
        }

        // 根据你的User实体结构，这里通常返回用户名（userId）
        // 如果你在JWT中存储的是username（即userId），直接返回authentication.getName()
        String username = authentication.getName();

        if (username == null || username.isEmpty() || "anonymousUser".equals(username)) {
            throw new RuntimeException("无法获取有效用户ID");
        }

        return username;
    }

}