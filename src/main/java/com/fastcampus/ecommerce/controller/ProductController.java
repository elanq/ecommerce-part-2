package com.fastcampus.ecommerce.controller;

import com.fastcampus.ecommerce.common.PageUtil;
import com.fastcampus.ecommerce.model.ActivityType;
import com.fastcampus.ecommerce.model.PaginatedProductResponse;
import com.fastcampus.ecommerce.model.ProductRequest;
import com.fastcampus.ecommerce.model.ProductResponse;
import com.fastcampus.ecommerce.model.ProductSearchRequest;
import com.fastcampus.ecommerce.model.SearchResponse;
import com.fastcampus.ecommerce.model.UserInfo;
import com.fastcampus.ecommerce.service.ProductService;
import com.fastcampus.ecommerce.service.SearchService;
import com.fastcampus.ecommerce.service.UserActivityService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("products")
@SecurityRequirement(name = "Bearer")
@RequiredArgsConstructor
public class ProductController {

  private final ProductService productService;
  private final SearchService searchService;
  private final UserActivityService userActivityService;

  // localhost:3000/products/2
  @GetMapping("/{id}")
  public ResponseEntity<ProductResponse> findProductById(
      @PathVariable(value = "id") Long productId) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    UserInfo userInfo = (UserInfo) authentication.getPrincipal();

    ProductResponse productResponse = productService.findById(productId);
    if (productResponse.getUserId() != userInfo.getUser().getUserId()) {
      userActivityService.trackProductView(productId, userInfo.getUser().getUserId());
    }
    return ResponseEntity.ok(productResponse);
  }

  // localhost:3000/products/
  @GetMapping("/{id}/similar")
  public ResponseEntity<SearchResponse<ProductResponse>> similarProducts(
      @PathVariable(value = "id") Long productId) {
    SearchResponse<ProductResponse> response = searchService.similarProducts(productId);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/search")
  public ResponseEntity<SearchResponse<ProductResponse>> search(
      @RequestBody ProductSearchRequest request) {
    SearchResponse<ProductResponse> response = searchService.search(request);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/recommendations")
  public ResponseEntity<SearchResponse<ProductResponse>> recommendations(
      @RequestParam(value = "user_activity", defaultValue = "VIEW") ActivityType activityType) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    UserInfo userInfo = (UserInfo) authentication.getPrincipal();

    SearchResponse<ProductResponse> response = searchService.userRecommendation(userInfo.getUser()
        .getUserId(), activityType);
    return ResponseEntity.ok(response);
  }


  // localhost:3000/products?page=0&size=10
  @GetMapping("")
  public ResponseEntity<PaginatedProductResponse> getAllProduct(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(defaultValue = "product_id,asc") String[] sort,
      @RequestParam(required = false) String name
  ) {
    List<Sort.Order> orders = PageUtil.parseSortOrderRequest(sort);
    Pageable pageable = PageRequest.of(page, size, Sort.by(orders));
    Page<ProductResponse> productResponses;

    if (name != null && !name.isEmpty()) {
      productResponses = productService.findByNameAndPageable(name, pageable);
    } else {
      productResponses = productService.findByPage(pageable);
    }

    return ResponseEntity.ok(productService.convertProductPage(productResponses));
  }

  @PostMapping("")
  public ResponseEntity<ProductResponse> createProduct(@RequestBody @Valid ProductRequest request) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    UserInfo userInfo = (UserInfo) authentication.getPrincipal();

    request.setUser(userInfo.getUser());
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
