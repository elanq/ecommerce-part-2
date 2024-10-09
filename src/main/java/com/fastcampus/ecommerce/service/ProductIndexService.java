package com.fastcampus.ecommerce.service;


import com.fastcampus.ecommerce.entity.Product;
import com.fastcampus.ecommerce.model.ActivityType;

public interface ProductIndexService {

  void reindexProduct(Product product);

  void deleteProduct(Product product);

  String indexName();

  void reindexProductActivity(Long productId, ActivityType activityType, Long value);
}
