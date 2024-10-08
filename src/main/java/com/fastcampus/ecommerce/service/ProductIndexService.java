package com.fastcampus.ecommerce.service;


import com.fastcampus.ecommerce.entity.Product;

public interface ProductIndexService {

  void reindexProduct(Product product);

  void deleteProduct(Product product);

  String indexName();
}
