package com.zjfc.smartgarbage.model.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "area_statistics")
@Data
public class AreaStatistics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stat_id")
    private Long statId;

    @Column(name = "area_name", length = 50, nullable = false)
    private String areaName;

    @Column(name = "area_type", length = 20)
    private String areaType; // DORMITORY, CLASSROOM, CAFETERIA, PLAYGROUND

    @Column(name = "total_deliveries")
    private Integer totalDeliveries = 0;

    @Column(name = "correct_deliveries")
    private Integer correctDeliveries = 0;

    @Column(name = "accuracy_rate")
    private Double accuracyRate = 0.0;

    @Column(name = "avg_points")
    private Double avgPoints = 0.0;

    @Column(name = "statistic_date", nullable = false)
    private LocalDate statisticDate;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}