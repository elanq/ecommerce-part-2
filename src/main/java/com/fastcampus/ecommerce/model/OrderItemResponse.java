package com.fastcampus.ecommerce.model;

import com.fastcampus.ecommerce.entity.OrderItem;
import com.fastcampus.ecommerce.entity.Product;
import com.fastcampus.ecommerce.entity.UserAddress;
import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(SnakeCaseStrategy.class)
public class OrderItemResponse implements Serializable {

  private Long orderItemId;
  private Long productId;
  private String productName;
  private BigDecimal price;
  private Integer quantity;
  private BigDecimal totalPrice;
  private UserAddressResponse shippingAddress;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  public static OrderItemResponse fromOrderItemProductAndAddress(
      OrderItem orderItem,
      Product product,
      UserAddress userAddress) {
    BigDecimal totalPrice = orderItem.getPrice()
        .multiply(BigDecimal.valueOf(orderItem.getQuantity()));

    return OrderItemResponse.builder()
        .orderItemId(orderItem.getOrderItemId())
        .productId(product.getProductId())
        .productName(product.getName())
        .price(orderItem.getPrice())
        .quantity(orderItem.getQuantity())
        .totalPrice(totalPrice)
        .shippingAddress(UserAddressResponse.fromUserAddress(userAddress))
        .createdAt(orderItem.getCreatedAt())
        .updatedAt(orderItem.getUpdatedAt())
        .build();
  }
}
