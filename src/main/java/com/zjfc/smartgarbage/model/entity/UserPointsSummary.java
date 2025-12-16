package com.zjfc.smartgarbage.model.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_points_summary")
@Data
public class UserPointsSummary {

    @Id
    @Column(name = "user_id", length = 20)
    private String userId;

    @Column(name = "total_points")
    private Integer totalPoints = 0;

    @Column(name = "available_points")
    private Integer availablePoints = 0;

    @Column(name = "frozen_points")
    private Integer frozenPoints = 0;

    @Column(name = "current_level", length = 20)
    private String currentLevel = "普通用户";

    @Column(name = "correct_count")
    private Integer correctCount = 0;

    @Column(name = "total_count")
    private Integer totalCount = 0;

    @Column(name = "accuracy_rate")
    private Double accuracyRate = 0.0;

    @Column(name = "last_delivery_time")
    private LocalDateTime lastDeliveryTime;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}