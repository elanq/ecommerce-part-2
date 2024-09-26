package com.fastcampus.ecommerce.service;

import com.fastcampus.ecommerce.model.ProductRequest;
import com.fastcampus.ecommerce.model.ProductResponse;
import java.util.List;
import java.util.Optional;

public interface ProductService {

  List<ProductResponse> findAll();

  ProductResponse findById(Long id);

  ProductResponse create(ProductRequest productRequest);

  ProductResponse update(Long id, ProductRequest productRequest);

  void delete(Long id);
}
