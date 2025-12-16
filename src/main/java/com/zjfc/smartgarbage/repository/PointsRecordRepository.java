package com.zjfc.smartgarbage.repository;

import com.zjfc.smartgarbage.model.entity.PointsRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PointsRecordRepository extends JpaRepository<PointsRecord, Long> {

    // 根据用户ID分页查询积分记录
    Page<PointsRecord> findByUserId(String userId, Pageable pageable);

    // 根据用户ID和变动类型查询积分记录
    List<PointsRecord> findByUserIdAndChangeType(String userId, String changeType);

    // 根据时间段查询积分记录
    List<PointsRecord> findByCreatedAtBetween(LocalDateTime startTime, LocalDateTime endTime);

    // 根据用户ID和时间段查询积分记录
    List<PointsRecord> findByUserIdAndCreatedAtBetween(String userId, LocalDateTime startTime, LocalDateTime endTime);

    // 统计用户积分总额（正负相加）
    @Query("SELECT COALESCE(SUM(p.pointsChange), 0) FROM PointsRecord p WHERE p.userId = :userId")
    Integer sumPointsByUser(@Param("userId") String userId);

    // 统计用户获得的总积分（只计算正数）
    @Query("SELECT COALESCE(SUM(p.pointsChange), 0) FROM PointsRecord p WHERE p.userId = :userId AND p.pointsChange > 0")
    Integer sumPositivePointsByUser(@Param("userId") String userId);

    // 统计用户消耗的总积分（只计算负数）
    @Query("SELECT COALESCE(SUM(p.pointsChange), 0) FROM PointsRecord p WHERE p.userId = :userId AND p.pointsChange < 0")
    Integer sumNegativePointsByUser(@Param("userId") String userId);

    // 按变动类型统计积分
    @Query("SELECT p.changeType, COUNT(p) as count, SUM(p.pointsChange) as total " +
            "FROM PointsRecord p " +
            "WHERE p.userId = :userId AND p.createdAt BETWEEN :start AND :end " +
            "GROUP BY p.changeType")
    List<Object[]> sumPointsByTypeAndUser(@Param("userId") String userId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    // 查找用户的最后一条积分记录
    PointsRecord findTopByUserIdOrderByCreatedAtDesc(String userId);
}