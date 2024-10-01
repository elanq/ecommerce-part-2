package com.fastcampus.ecommerce.service;

import com.fastcampus.ecommerce.entity.Order;
import com.fastcampus.ecommerce.model.CheckoutRequest;
import com.fastcampus.ecommerce.model.OrderItemResponse;
import com.fastcampus.ecommerce.model.OrderResponse;
import com.fastcampus.ecommerce.model.OrderStatus;
import com.fastcampus.ecommerce.model.PaginatedOrderResponse;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {

  OrderResponse checkout(CheckoutRequest checkoutRequest);

  Optional<Order> findOrderById(Long orderId);

  List<Order> findOrdersByUserId(Long userId);

  Page<OrderResponse> findOrdersByUserIdAndPageable(Long userId, Pageable pageable);

  List<Order> findOrdersByStatus(OrderStatus status);

  void cancelOrder(Long orderId);

  List<OrderItemResponse> findOrderItemsByOrderId(Long orderId);

  void updateOrderStatus(Long orderId, OrderStatus newStatus);

  Double calculateOrderTotal(Long orderId);

  PaginatedOrderResponse convertOrderPage(Page<OrderResponse> orderResponses);
}
