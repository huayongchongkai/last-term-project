package com.zjfc.smartgarbage.model.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "points_record")
@Data
public class PointsRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "record_id")
    private Long recordId;

    @Column(name = "user_id", length = 20, nullable = false)
    private String userId;

    @Column(name = "points_change", nullable = false)
    private Integer pointsChange;

    @Column(name = "change_type", length = 20, nullable = false)
    private String changeType; // DELIVERY, SHARE, EXCHANGE, CORRECTION

    @Column(name = "change_reason", length = 100)
    private String changeReason;

    @Column(name = "related_id", length = 50)
    private String relatedId;

    @Column(name = "remark", length = 200)
    private String remark;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}