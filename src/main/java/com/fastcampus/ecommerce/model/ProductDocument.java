package com.fastcampus.ecommerce.model;

import com.fastcampus.ecommerce.entity.Category;
import com.fastcampus.ecommerce.entity.Product;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDocument {

  private String id;
  private String name;
  private String description;
  private BigDecimal price;
  private BigDecimal weight;
  private Integer stockQuantity;
  private Long userId;
  private Long purchaseCount;
  private Long viewCount;
  private List<CategoryInfo> categories;
  private String nameSuggest;
  private String nameNgram;

  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
  private LocalDateTime createdAt;

  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
  private LocalDateTime updatedAt;

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  public static class CategoryInfo {

    private Long categoryId;
    private String name;
  }

  public static ProductDocument fromProductAndCategories(Product product,
      List<Category> categories) {
    ProductDocument productDocument = ProductDocument.builder()
        .id(String.valueOf(product.getProductId()))
        .name(product.getName())
        .description(product.getDescription())
        .price(product.getPrice())
        .weight(product.getWeight())
        .stockQuantity(product.getStockQuantity())
        .userId(product.getUserId())
        .nameSuggest(product.getName())
        .nameNgram(product.getName())
        .build();
    List<CategoryInfo> categoryInfos = categories.stream().map(category -> {
      CategoryInfo categoryInfo = new CategoryInfo();
      categoryInfo.setCategoryId(category.getCategoryId());
      categoryInfo.setName(category.getName());
      return categoryInfo;
    }).toList();
    productDocument.setCategories(categoryInfos);
    return productDocument;
  }
}
