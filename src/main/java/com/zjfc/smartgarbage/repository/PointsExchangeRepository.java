package com.zjfc.smartgarbage.repository;

import com.zjfc.smartgarbage.model.entity.PointsExchange;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PointsExchangeRepository extends JpaRepository<PointsExchange, Long> {

    // 根据用户ID分页查询兑换记录
    Page<PointsExchange> findByUserId(String userId, Pageable pageable);

    // 根据用户ID查询所有兑换记录
    List<PointsExchange> findByUserId(String userId);

    // 根据状态查询兑换记录 - 修正：改为 findByExchangeStatus
    List<PointsExchange> findByExchangeStatus(String exchangeStatus);

    // 根据用户ID和状态查询 - 修正：改为 findByUserIdAndExchangeStatus
    List<PointsExchange> findByUserIdAndExchangeStatus(String userId, String exchangeStatus);

    // 根据时间段查询兑换记录
    List<PointsExchange> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    // 统计用户兑换次数
    Long countByUserId(String userId);

    // 统计用户消耗的总积分 - 修正：pointsCost 而不是 pointsRequired
    @Query("SELECT COALESCE(SUM(pe.pointsCost), 0) FROM PointsExchange pe WHERE pe.userId = :userId")
    Integer sumPointsByUser(@Param("userId") String userId);

    // 查找最近的兑换记录
    PointsExchange findTopByUserIdOrderByCreatedAtDesc(String userId);
}