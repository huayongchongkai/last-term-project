package com.zjfc.smartgarbage.model.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "garbage_category")
@Data
public class GarbageCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Integer categoryId;

    @Column(name = "category_code", length = 10, unique = true, nullable = false)
    private String categoryCode;

    @Column(name = "category_name", length = 50, nullable = false)
    private String categoryName;

    @Column(name = "color", length = 20)
    private String color;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "disposal_guide", columnDefinition = "TEXT")
    private String disposalGuide;

    @Column(name = "examples", columnDefinition = "TEXT")
    private String examples;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

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