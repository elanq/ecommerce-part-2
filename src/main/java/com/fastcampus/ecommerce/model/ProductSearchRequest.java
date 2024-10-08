package com.fastcampus.ecommerce.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class ProductSearchRequest {

  private String query;
  private String category;
  private Double minPrice;
  private Double maxPrice;
  private String sortBy = "_score";
  private String sortOrder;
  private int page;
  private int size;
}
