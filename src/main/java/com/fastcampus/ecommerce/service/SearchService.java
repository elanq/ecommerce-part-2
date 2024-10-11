package com.fastcampus.ecommerce.service;

import com.fastcampus.ecommerce.model.ActivityType;
import com.fastcampus.ecommerce.model.ProductResponse;
import com.fastcampus.ecommerce.model.ProductSearchRequest;
import com.fastcampus.ecommerce.model.SearchResponse;
import java.util.List;

public interface SearchService {

  SearchResponse<ProductResponse> search(ProductSearchRequest searchRequest);

  SearchResponse<ProductResponse> similarProducts(Long productId);

  SearchResponse<ProductResponse> userRecommendation(Long userId, ActivityType activityType);

  List<String> getAutocomplete(String query);

  List<String> getNgramAutocomplete(String query);

  List<String> getFuzzyAutocomplete(String query);

  List<String> combinedAutocomplete(String query);
}
