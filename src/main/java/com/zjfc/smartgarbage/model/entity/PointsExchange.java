package com.zjfc.smartgarbage.model.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "points_exchange")
@Data
public class PointsExchange {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "exchange_id")
    private Long exchangeId;

    @Column(name = "user_id", length = 20, nullable = false)
    private String userId;

    @Column(name = "item_name", length = 100, nullable = false)
    private String itemName;

    @Column(name = "item_type", length = 20)
    private String itemType; // GIFT, COUPON, PRIVILEGE

    @Column(name = "points_cost", nullable = false)
    private Integer pointsCost;

    @Column(name = "exchange_status", length = 20)
    private String exchangeStatus = "PENDING"; // PENDING, PROCESSING, COMPLETED, CANCELLED

    @Column(name = "delivery_info", columnDefinition = "TEXT")
    private String deliveryInfo;

    @Column(name = "exchange_time")
    private LocalDateTime exchangeTime;

    @Column(name = "completed_time")
    private LocalDateTime completedTime;

    @Column(name = "remark", length = 200)
    private String remark;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}