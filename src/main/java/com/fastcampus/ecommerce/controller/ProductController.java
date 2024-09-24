package com.fastcampus.ecommerce.controller;

import com.fastcampus.ecommerce.model.ProductRequest;
import com.fastcampus.ecommerce.model.ProductResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("products")
@SecurityRequirement(name = "Bearer")
public class ProductController {

  // localhost:3000/products/2
  @GetMapping("/{id}")
  public ResponseEntity<ProductResponse> findProductById(
      @PathVariable(value = "id") Long productId) {
    return ResponseEntity.ok(
        ProductResponse.builder()
            .name("product" + productId)
            .price(BigDecimal.ONE)
            .description("deskripsi produk")
            .build()
    );
  }

  // localhost:3000/products
  @GetMapping("")
  public ResponseEntity<List<ProductResponse>> getAllProduct() {
    return ResponseEntity.ok(
        List.of(
        ProductResponse.builder()
            .name("product 1")
            .price(BigDecimal.ONE)
            .description("deskripsi produk")
            .build(),
            ProductResponse.builder()
                .name("product 1")
                .price(BigDecimal.ONE)
                .description("deskripsi produk")
                .build()
        )
    );
  }

  @PostMapping("")
  public ResponseEntity<ProductResponse> createProduct(@RequestBody @Valid ProductRequest request) {
    return ResponseEntity.ok(
        ProductResponse.builder()
            .name(request.getName())
            .price(request.getPrice())
            .description(request.getDescription())
            .build()
    );
  }

  @PutMapping("/{id}")
  public ResponseEntity<ProductResponse> updateProduct(
      @RequestBody @Valid ProductRequest request,
      @PathVariable(name = "id") Long productID
  ) {
    return ResponseEntity.ok(
        ProductResponse.builder()
            .name(request.getName() + " " + productID)
            .price(request.getPrice())
            .description(request.getDescription())
            .build()
    );
  }
}
