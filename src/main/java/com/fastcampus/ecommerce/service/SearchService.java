package com.fastcampus.ecommerce.service;

import com.fastcampus.ecommerce.model.ProductResponse;
import com.fastcampus.ecommerce.model.ProductSearchRequest;
import com.fastcampus.ecommerce.model.SearchResponse;

public interface SearchService {

  SearchResponse<ProductResponse> search(ProductSearchRequest searchRequest);
}
