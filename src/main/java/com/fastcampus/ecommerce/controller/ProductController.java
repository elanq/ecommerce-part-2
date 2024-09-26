package com.fastcampus.ecommerce.controller;

import com.fastcampus.ecommerce.entity.Product;
import com.fastcampus.ecommerce.model.ProductRequest;
import com.fastcampus.ecommerce.model.ProductResponse;
import com.fastcampus.ecommerce.repository.ProductRepository;
import com.fastcampus.ecommerce.service.ProductService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
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
@RequiredArgsConstructor
public class ProductController {

  private final ProductService productService;

  // localhost:3000/products/2
  @GetMapping("/{id}")
  public ResponseEntity<ProductResponse> findProductById(
      @PathVariable(value = "id") Long productId) {
    ProductResponse productResponse = productService.findById(productId);
    return ResponseEntity.ok(productResponse);
  }

  // localhost:3000/products
  @GetMapping("")
  public ResponseEntity<List<ProductResponse>> getAllProduct() {
    List<ProductResponse> productResponses = productService.findAll();
    return ResponseEntity.ok(productResponses);
  }

  @PostMapping("")
  public ResponseEntity<ProductResponse> createProduct(@RequestBody @Valid ProductRequest request) {
    ProductResponse response = productService.create(request);
    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(response);
  }

  @PutMapping("/{id}")
  public ResponseEntity<ProductResponse> updateProduct(
      @RequestBody @Valid ProductRequest request,
      @PathVariable(name = "id") Long productID
  ) {
    ProductResponse response = productService.update(productID, request);
    return ResponseEntity.ok(response);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteProduct(
      @PathVariable(name = "id") Long productID
  ) {
    productService.delete(productID);
    return ResponseEntity.noContent().build();
  }
}
