package com.zjfc.smartgarbage.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 积分排行榜视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaderboardVO {

    /**
     * 排名
     */
    private Integer rank;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 学号
     */
    private String studentId;

    /**
     * 总积分
     */
    private Integer totalPoints;

    /**
     * 可用积分
     */
    private Integer availablePoints;

    /**
     * 等级名称
     */
    private String levelName;

    /**
     * 正确率（百分比）
     */
    private Double accuracyRate;

    /**
     * 头像URL
     */
    private String avatarUrl;

    /**
     * 正确投放次数
     */
    private Integer correctCount;

    /**
     * 总投放次数
     */
    private Integer totalCount;
}