package com.zjfc.smartgarbage.model.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "delivery_record")
@Data
public class DeliveryRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "record_id")
    private Long recordId;

    @Column(name = "user_id", length = 20, nullable = false)
    private String userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", referencedColumnName = "category_id")
    private GarbageCategory garbageCategory;

    @Column(name = "terminal_id", length = 50)
    private String terminalId;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "confidence")
    private Double confidence;

    @Column(name = "is_correct")
    private Boolean isCorrect = false;

    @Column(name = "points_awarded")
    private Integer pointsAwarded = 0;

    @Column(name = "location", length = 100)
    private String location;

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