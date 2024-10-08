package com.fastcampus.ecommerce.config;

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import java.io.IOException;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ElasticsearchIndexRetrierConfig {

  @Value("${elasticsearch.index.retrier.max-attempts:3}")
  private Integer maxAttempts;

  @Value("${elasticsearch.index.retrier.wait-duration:5s}")
  private Duration waitDuration;

  @Bean
  public Retry elasticsearchIndexRetrier() {
    RetryConfig config = RetryConfig.custom()
        .maxAttempts(maxAttempts)
        .waitDuration(waitDuration)
        .retryExceptions(IOException.class)
        .build();
    return Retry.of("elasticsearchIndexRetrier", config);
  }
}
