package com.zjfc.smartgarbage.repository;

import com.zjfc.smartgarbage.model.entity.UserPointsSummary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Repository
public interface UserPointsSummaryRepository extends JpaRepository<UserPointsSummary, String> {

        // 根据用户ID查询积分汇总
        UserPointsSummary findByUserId(String userId);

        // 根据积分范围查询用户（用于排行榜）
        Page<UserPointsSummary> findByTotalPointsBetween(Integer minPoints, Integer maxPoints, Pageable pageable);

        // 根据等级查询用户
        List<UserPointsSummary> findByCurrentLevel(String level);

        // 按积分降序排列（排行榜）
        Page<UserPointsSummary> findAllByOrderByTotalPointsDesc(Pageable pageable);

        // 按准确率降序排列
        Page<UserPointsSummary> findAllByOrderByAccuracyRateDesc(Pageable pageable);

        // 按宿舍区域查询用户积分情况
        @Query("SELECT ups FROM UserPointsSummary ups, User u " +
                        "WHERE ups.userId = u.userId AND u.dormitory = :dormitory " +
                        "ORDER BY ups.totalPoints DESC")
        List<UserPointsSummary> findByDormitory(@Param("dormitory") String dormitory);

        // 更新用户积分（原子操作）
        @Modifying
        @Transactional
        @Query("UPDATE UserPointsSummary ups SET ups.totalPoints = ups.totalPoints + :points, " +
                        "ups.availablePoints = ups.availablePoints + :points, " +
                        "ups.updatedAt = CURRENT_TIMESTAMP " +
                        "WHERE ups.userId = :userId")
        int addPoints(@Param("userId") String userId, @Param("points") Integer points);

        // 消耗用户积分（原子操作）
        @Modifying
        @Transactional
        @Query("UPDATE UserPointsSummary ups SET ups.totalPoints = ups.totalPoints - :points, " +
                        "ups.availablePoints = ups.availablePoints - :points, " +
                        "ups.updatedAt = CURRENT_TIMESTAMP " +
                        "WHERE ups.userId = :userId AND ups.availablePoints >= :points")
        int deductPoints(@Param("userId") String userId, @Param("points") Integer points);

        // 更新用户正确投递次数和准确率
        @Modifying
        @Transactional
        @Query(value = "UPDATE user_points_summary ups " +
                        "SET ups.correct_count = ups.correct_count + :correctIncrement, " +
                        "    ups.total_count = ups.total_count + :totalIncrement, " +
                        "    ups.accuracy_rate = CASE " +
                        "        WHEN (ups.total_count + :totalIncrement) > 0 " +
                        "        THEN (ups.correct_count + :correctIncrement) / (ups.total_count + :totalIncrement) " +
                        "        ELSE 0.0 " +
                        "    END, " +
                        "    ups.last_delivery_time = NOW(), " +
                        "    ups.updated_at = NOW() " +
                        "WHERE ups.user_id = :userId", nativeQuery = true) // 注意：nativeQuery = true 并且使用数据库列名
        int updateDeliveryStats(@Param("userId") String userId,
                        @Param("correctIncrement") Integer correctIncrement,
                        @Param("totalIncrement") Integer totalIncrement);

        // 获取积分排行榜前N名
        @Query(value = "SELECT ups FROM UserPointsSummary ups ORDER BY ups.totalPoints DESC")
        List<UserPointsSummary> findTopN(Pageable pageable);
}