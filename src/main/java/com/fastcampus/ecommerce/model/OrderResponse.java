package com.fastcampus.ecommerce.model;

import com.fastcampus.ecommerce.entity.Order;
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
public class OrderResponse implements Serializable {

  private Long orderId;
  private Long userId;
  private BigDecimal subtotal;
  private BigDecimal shippingFee;
  private BigDecimal taxFee;
  private BigDecimal totalAmount;
  private String status;
  private LocalDateTime orderDate;

  public static OrderResponse fromOrder(Order order) {
    return OrderResponse.builder()
        .orderId(order.getOrderId())
        .userId(order.getUserId())
        .subtotal(order.getSubtotal())
        .shippingFee(order.getShippingFee())
        .taxFee(order.getTaxFee())
        .totalAmount(order.getTotalAmount())
        .status(order.getStatus())
        .orderDate(order.getOrderDate())
        .build();
  }
}
