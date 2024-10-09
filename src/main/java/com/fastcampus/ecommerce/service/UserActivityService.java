package com.fastcampus.ecommerce.service;

import com.fastcampus.ecommerce.entity.UserActivity;
import com.fastcampus.ecommerce.model.ActivityType;
import java.time.LocalDateTime;
import java.util.List;

public interface UserActivityService {

  long getActivityCount(Long productId, ActivityType activityType);

  long getActivityCountInDateRange(Long productId, ActivityType activityType, LocalDateTime start,
      LocalDateTime end);

  void trackPurchase(Long productId, Long userId);

  void trackProductView(Long productId, Long userId);

  List<UserActivity> getLastMonthUserPurchase(Long userId);

  List<UserActivity> getLastMonthUserView(Long userId);

}
