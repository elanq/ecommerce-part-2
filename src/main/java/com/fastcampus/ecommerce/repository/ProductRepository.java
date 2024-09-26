package com.fastcampus.ecommerce.repository;

import com.fastcampus.ecommerce.entity.Product;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

  // SELECT * FROM products WHERE name = ?;
  @Query(value = """
      SELECT * FROM product
      WHERE lower("name") like :name
      """, nativeQuery = true)
  List<Product> findByName(String name);

  @Query(value = """
      SELECT DISTINCT p.* FROM product p
      JOIN product_category pc ON p.product_id = pc.product_id
      JOIN category c ON pc.category_id = c.category_id
      WHERE c.name = :categoryName
      """, nativeQuery = true)
  List<Product> findByCategory(@Param("categoryName") String categoryName);
}
