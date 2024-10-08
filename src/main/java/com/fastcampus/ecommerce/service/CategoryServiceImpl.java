package com.fastcampus.ecommerce.service;

import com.fastcampus.ecommerce.entity.Category;
import com.fastcampus.ecommerce.repository.CategoryRepository;
import com.fastcampus.ecommerce.repository.ProductCategoryRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceImpl implements
    CategoryService {

  private final ProductCategoryRepository productCategoryRepository;
  private final CategoryRepository categoryRepository;

  @Override
  public List<Category> getProductCategories(Long productId) {
    List<Long> categoryIds = productCategoryRepository.findCategoriesByProductId(
            productId)
        .stream()
        .map(productCategory -> productCategory.getId().getCategoryId()).toList();
    return categoryRepository.findAllById(categoryIds);
  }
}
