package com.fastcampus.ecommerce.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "product_category")
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductCategory {

  @EmbeddedId
  private ProductCategoryId id;

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @Embeddable
  public static class ProductCategoryId {
    @Column(name = "product_id")
    private Long productId;
    @Column(name = "category_id")
    private Long categoryId;
  }
}
