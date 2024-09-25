package com.fastcampus.ecommerce.repository;

import com.fastcampus.ecommerce.entity.ProductCategory;
import com.fastcampus.ecommerce.entity.ProductCategory.ProductCategoryId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductCategoryRepository extends JpaRepository<ProductCategory, ProductCategoryId> {

}
