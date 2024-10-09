package com.fastcampus.ecommerce.service;

import com.fastcampus.ecommerce.entity.UserActivity;
import com.fastcampus.ecommerce.model.ActivityType;
import com.fastcampus.ecommerce.repository.UserActivityRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserActivityServiceImpl implements
    UserActivityService {

  private final UserActivityRepository userActivityRepository;
  private final ProductIndexService productIndexService;

  @Override
  public long getActivityCount(Long productId, ActivityType activityType) {
    return userActivityRepository.countProductActivityByType(productId, activityType.name());
  }

  @Override
  public long getActivityCountInDateRange(Long productId, ActivityType activityType,
      LocalDateTime start, LocalDateTime end) {
    return userActivityRepository.countProductActivityByTypeAndDateRange(productId,
        activityType.name(), start, end);
  }

  @Override
  @Async
  @Transactional
  public void trackPurchase(Long productId, Long userId) {
    UserActivity userActivity = UserActivity.builder()
        .productId(productId)
        .userId(userId)
        .activityType(ActivityType.PURCHASE)
        .build();
    userActivityRepository.save(userActivity);

    Long purchaseCount = getActivityCount(productId, ActivityType.PURCHASE);
    productIndexService.reindexProductActivity(productId, ActivityType.PURCHASE, purchaseCount);
  }

  @Override
  @Async
  @Transactional
  public void trackProductView(Long productId, Long userId) {
    UserActivity userActivity = UserActivity.builder()
        .productId(productId)
        .userId(userId)
        .activityType(ActivityType.VIEW)
        .build();
    userActivityRepository.save(userActivity);

    Long viewCount = getActivityCount(productId, ActivityType.VIEW);
    productIndexService.reindexProductActivity(productId, ActivityType.VIEW, viewCount);

  }

  @Override
  public List<UserActivity> getLastMonthUserPurchase(Long userId) {
    LocalDateTime from = LocalDateTime.now().minusMonths(1);
    LocalDateTime to = LocalDateTime.now();

    return userActivityRepository.findByUserIdAndType(userId, ActivityType.PURCHASE.name(), from,
        to);
  }

  @Override
  public List<UserActivity> getLastMonthUserView(Long userId) {
    LocalDateTime from = LocalDateTime.now().minusMonths(1);
    LocalDateTime to = LocalDateTime.now();

    return userActivityRepository.findByUserIdAndType(userId, ActivityType.VIEW.name(), from, to);
  }
}
