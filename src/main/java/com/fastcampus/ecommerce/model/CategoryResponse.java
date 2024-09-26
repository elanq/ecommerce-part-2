package com.fastcampus.ecommerce.model;

import com.fastcampus.ecommerce.entity.Category;
import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(SnakeCaseStrategy.class)
public class CategoryResponse {
  private Long categoryId;
  private String name;

  public static CategoryResponse fromCategory(Category category) {
    return CategoryResponse.builder()
        .categoryId(category.getCategoryId())
        .name(category.getName())
        .build();
  }
}
