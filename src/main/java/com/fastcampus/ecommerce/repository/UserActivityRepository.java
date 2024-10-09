package com.fastcampus.ecommerce.repository;

import com.fastcampus.ecommerce.entity.UserActivity;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserActivityRepository extends JpaRepository<UserActivity, Long> {

  // Count product activity by its activity type
  @Query(value = "SELECT COUNT(*) FROM user_activity " +
      "WHERE product_id = :productId " +
      "AND activity_type = :activityType",
      nativeQuery = true)
  long countProductActivityByType(
      @Param("productId") Long productId,
      @Param("activityType") String activityType);

  // Count product activity by its activity type within a certain date range
  @Query(value = "SELECT COUNT(*) FROM user_activity " +
      "WHERE product_id = :productId " +
      "AND activity_type = :activityType " +
      "AND created_at BETWEEN :startDate AND :endDate",
      nativeQuery = true)
  long countProductActivityByTypeAndDateRange(
      @Param("productId") Long productId,
      @Param("activityType") String activityType,
      @Param("startDate") LocalDateTime startDate,
      @Param("endDate") LocalDateTime endDate);

  @Query(value = """
      SELECT * FROM user_activity
      WHERE user_id = :userId
      AND activity_type = :activityType
      AND created_at BETWEEN :startDate AND :endDate
      """, nativeQuery = true)
  List<UserActivity> findByUserIdAndType(
      @Param("userId") Long userId,
      @Param("activityType") String activityType,
      @Param("startDate") LocalDateTime startDate,
      @Param("endDate") LocalDateTime endDate);

}
