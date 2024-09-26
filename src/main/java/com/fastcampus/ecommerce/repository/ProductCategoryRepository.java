package com.fastcampus.ecommerce.repository;

import com.fastcampus.ecommerce.entity.ProductCategory;
import com.fastcampus.ecommerce.entity.ProductCategory.ProductCategoryId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductCategoryRepository extends JpaRepository<ProductCategory, ProductCategoryId> {

  @Query(value = """
      SELECT * FROM product_category
      WHERE product_id = :productId
      """, nativeQuery = true)
  List<ProductCategory> findCategoriesByProductId(@Param("productId") Long productId);
}
