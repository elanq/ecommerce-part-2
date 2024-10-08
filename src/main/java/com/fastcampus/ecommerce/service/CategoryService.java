package com.fastcampus.ecommerce.service;

import com.fastcampus.ecommerce.entity.Category;
import java.util.List;

public interface CategoryService {

  List<Category> getProductCategories(Long productId);
}
