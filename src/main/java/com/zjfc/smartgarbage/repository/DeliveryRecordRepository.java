package com.zjfc.smartgarbage.repository;

import com.zjfc.smartgarbage.model.entity.DeliveryRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DeliveryRecordRepository extends JpaRepository<DeliveryRecord, Long> {

    // 根据用户ID分页查询投递记录
    Page<DeliveryRecord> findByUserId(String userId, Pageable pageable);

    // 根据用户ID和是否正确查询投递记录
    List<DeliveryRecord> findByUserIdAndIsCorrect(String userId, Boolean isCorrect);

    // 根据时间段查询投递记录
    List<DeliveryRecord> findByCreatedAtBetween(LocalDateTime startTime, LocalDateTime endTime);

    // 根据区域位置查询投递记录
    List<DeliveryRecord> findByLocation(String location);

    // 统计用户正确投递次数
    @Query("SELECT COUNT(d) FROM DeliveryRecord d WHERE d.userId = :userId AND d.isCorrect = true")
    Long countCorrectDeliveriesByUser(@Param("userId") String userId);

    // 统计用户总投递次数
    @Query("SELECT COUNT(d) FROM DeliveryRecord d WHERE d.userId = :userId")
    Long countTotalDeliveriesByUser(@Param("userId") String userId);

    // 按时间段统计分类准确率
    @Query("SELECT AVG(CASE WHEN d.isCorrect = true THEN 1.0 ELSE 0.0 END) " +
            "FROM DeliveryRecord d WHERE d.createdAt BETWEEN :start AND :end")
    Double calculateAccuracyRate(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // 统计各区域投递量
    @Query("SELECT d.location, COUNT(d) as count " +
            "FROM DeliveryRecord d " +
            "WHERE d.location IS NOT NULL AND d.createdAt BETWEEN :start AND :end " +
            "GROUP BY d.location")
    List<Object[]> countDeliveriesByLocation(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // 统计各类垃圾的投递量
    @Query("SELECT gc.categoryName, COUNT(d) as count " +
            "FROM DeliveryRecord d JOIN d.garbageCategory gc " +
            "WHERE d.createdAt BETWEEN :start AND :end " +
            "GROUP BY gc.categoryName")
    List<Object[]> countDeliveriesByCategory(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // 查找最近N条投递记录
    List<DeliveryRecord> findTop10ByOrderByCreatedAtDesc();
}