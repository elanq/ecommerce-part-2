package com.fastcampus.ecommerce.service;

import com.fastcampus.ecommerce.entity.Order;
import com.fastcampus.ecommerce.model.CheckoutRequest;
import com.fastcampus.ecommerce.model.OrderItemResponse;
import java.util.List;
import java.util.Optional;

public interface OrderService {

  Order checkout(CheckoutRequest checkoutRequest);

  Optional<Order> findOrderById(Long orderId);

  List<Order> findOrdersByUserId(Long userId);

  List<Order> findOrdersByStatus(String status);

  void cancelOrder(Long orderId);

  List<OrderItemResponse> findOrderItemsByOrderId(Long orderId);

  void updateOrderStatus(Long orderId, String newStatus);

  Double calculateOrderTotal(Long orderId);
}
