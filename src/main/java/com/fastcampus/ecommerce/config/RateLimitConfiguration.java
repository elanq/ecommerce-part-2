package com.fastcampus.ecommerce.config;

import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RateLimitConfiguration {

  @Value("${rate.limit.default:100}")
  private int defaultLimitForPeriod;
  @Value("${rate.limit.period:60}")
  private int limitRefreshPeriodInSeconds;
  @Value("${rate.limit.timeout:1}")
  private int timeoutInSeconds;

  @Bean
  public RateLimiterConfig rateLimiterConfig() {
    return RateLimiterConfig.custom()
        .limitForPeriod(defaultLimitForPeriod)
        .limitRefreshPeriod(Duration.ofSeconds(limitRefreshPeriodInSeconds))
        .timeoutDuration(Duration.ofSeconds(timeoutInSeconds))
        .build();
  }

  @Bean
  public RateLimiterRegistry rateLimiterRegistry(RateLimiterConfig rateLimiterConfig) {
    return RateLimiterRegistry.of(rateLimiterConfig);
  }
}
