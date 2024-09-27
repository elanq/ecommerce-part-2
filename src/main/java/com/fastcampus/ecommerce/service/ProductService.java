package com.fastcampus.ecommerce.service;

import com.fastcampus.ecommerce.entity.Product;
import com.fastcampus.ecommerce.model.PaginatedProductResponse;
import com.fastcampus.ecommerce.model.ProductRequest;
import com.fastcampus.ecommerce.model.ProductResponse;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductService {

  List<ProductResponse> findAll();

  Page<ProductResponse> findByPage(Pageable pageable);

  Page<ProductResponse> findByNameAndPageable(String name, Pageable pageable);

  ProductResponse findById(Long id);

  ProductResponse create(ProductRequest productRequest);

  ProductResponse update(Long id, ProductRequest productRequest);

  void delete(Long id);

  PaginatedProductResponse convertProductPage(Page<ProductResponse> responses);
}
