package com.fastcampus.ecommerce.service;

import com.fastcampus.ecommerce.common.errors.ResourceNotFoundException;
import com.fastcampus.ecommerce.entity.CartItem;
import com.fastcampus.ecommerce.entity.Order;
import com.fastcampus.ecommerce.entity.OrderItem;
import com.fastcampus.ecommerce.entity.Product;
import com.fastcampus.ecommerce.entity.UserAddress;
import com.fastcampus.ecommerce.model.CheckoutRequest;
import com.fastcampus.ecommerce.model.OrderItemResponse;
import com.fastcampus.ecommerce.model.OrderResponse;
import com.fastcampus.ecommerce.model.PaymentResponse;
import com.fastcampus.ecommerce.model.ShippingRateRequest;
import com.fastcampus.ecommerce.model.ShippingRateResponse;
import com.fastcampus.ecommerce.repository.CartItemRepository;
import com.fastcampus.ecommerce.repository.OrderItemRepository;
import com.fastcampus.ecommerce.repository.OrderRepository;
import com.fastcampus.ecommerce.repository.ProductRepository;
import com.fastcampus.ecommerce.repository.UserAddressRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

  private final CartItemRepository cartItemRepository;
  private final OrderRepository orderRepository;
  private final OrderItemRepository orderItemRepository;
  private final UserAddressRepository userAddressRepository;
  private final ProductRepository productRepository;
  private final ShippingService shippingService;
  private final PaymentService paymentService;

  private final BigDecimal TAX_RATE = BigDecimal.valueOf(0.03);

  @Override
  @Transactional
  public OrderResponse checkout(CheckoutRequest checkoutRequest) {
    List<CartItem> selectedItems = cartItemRepository.findAllById(
        checkoutRequest.getSelectedCartItemIds());
    if (selectedItems.isEmpty()) {
      throw new ResourceNotFoundException("No cart items found for checkout");
    }

    UserAddress shippingAddress = userAddressRepository.findById(checkoutRequest.getUserAddressId())
        .orElseThrow(() -> new ResourceNotFoundException(
            "Shipping address with id " + checkoutRequest.getUserAddressId() + " is not found"));

    // request is validated

    Order newOrder = Order.builder()
        .userId(checkoutRequest.getUserId())
        .status("PENDING")
        .orderDate(LocalDateTime.now())
        .totalAmount(BigDecimal.ZERO)
        .taxFee(BigDecimal.ZERO)
        .subtotal(BigDecimal.ZERO)
        .shippingFee(BigDecimal.ZERO)
        .build();

    Order savedOrder = orderRepository.save(newOrder);

    List<OrderItem> orderItems = selectedItems.stream()
        .map(cartItem -> {
          return OrderItem.builder()
              .orderId(savedOrder.getOrderId())
              .productId(cartItem.getProductId())
              .quantity(cartItem.getQuantity())
              .price(cartItem.getPrice())
              .userAddressId(shippingAddress.getUserAddressId())
              .build();
        })
        .toList();

    orderItemRepository.saveAll(orderItems);

    cartItemRepository.deleteAll(selectedItems);

    BigDecimal subtotal = orderItems.stream()
        .map(
            orderItem -> orderItem.getPrice().multiply(BigDecimal.valueOf(orderItem.getQuantity())))
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    BigDecimal shippingFee = orderItems.stream()
        .map(orderItem -> {
          Optional<Product> product = productRepository.findById(orderItem.getProductId());
          if (product.isEmpty()) {
            return BigDecimal.ZERO;
          }

          Optional<UserAddress> sellerAddress = userAddressRepository.findByUserIdAndIsDefaultTrue(
              product.get()
                  .getUserId());
          if (sellerAddress.isEmpty()) {
            return BigDecimal.ZERO;
          }

          BigDecimal totalWeight = product.get().getWeight()
              .multiply(BigDecimal.valueOf(orderItem.getQuantity()));
          // calculate shipping rate
          ShippingRateRequest rateRequest = ShippingRateRequest.builder()
              .totalWeightInGrams(totalWeight)
              .fromAddress(ShippingRateRequest.fromUserAddress(sellerAddress.get()))
              .toAddress(ShippingRateRequest.fromUserAddress(shippingAddress))
              .build();
          ShippingRateResponse rateResponse = shippingService.calculateShippingRate(rateRequest);
          return rateResponse.getShippingFee();
        })
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    BigDecimal taxFee = subtotal.multiply(TAX_RATE);
    BigDecimal totalAmount = subtotal.add(taxFee).add(shippingFee);

    savedOrder.setSubtotal(subtotal);
    savedOrder.setShippingFee(shippingFee);
    savedOrder.setTaxFee(taxFee);
    savedOrder.setTotalAmount(totalAmount);

    orderRepository.save(savedOrder);

    // interact with xendit api
    // generate payment url
    String paymentUrl;

    try {
      PaymentResponse paymentResponse = paymentService.create(savedOrder);
      savedOrder.setXenditInvoiceId(paymentResponse.getXenditInvoiceId());
      savedOrder.setXenditPaymentStatus(paymentResponse.getXenditInvoiceStatus());
      paymentUrl = paymentResponse.getXenditPaymentUrl();

      orderRepository.save(savedOrder);
    } catch (Exception ex) {
      log.error("Payment creation for order: " + savedOrder.getOrderId() + " is failed. Reason:"
          + ex.getMessage());
      savedOrder.setStatus("PAYMENT_FAILED");

      orderRepository.save(savedOrder);
      return OrderResponse.fromOrder(savedOrder);
    }

    OrderResponse orderResponse = OrderResponse.fromOrder(savedOrder);
    orderResponse.setPaymentUrl(paymentUrl);
    return orderResponse;
  }

  @Override
  public Optional<Order> findOrderById(Long orderId) {
    return orderRepository.findById(orderId);
  }

  @Override
  public List<Order> findOrdersByUserId(Long userId) {
    return orderRepository.findByUserId(userId);
  }

  @Override
  public List<Order> findOrdersByStatus(String status) {
    return orderRepository.findByStatus(status);
  }

  @Override
  @Transactional
  public void cancelOrder(Long orderId) {
    Order order = orderRepository.findById(orderId)
        .orElseThrow(
            () -> new ResourceNotFoundException("Order with id " + orderId + " not found"));

    if (!"PENDING".equals(order.getStatus())) {
      throw new IllegalStateException("Only PENDING orders can be cancelled");
    }

    order.setStatus("CANCELLED");
    orderRepository.save(order);

  }

  @Override
  public List<OrderItemResponse> findOrderItemsByOrderId(Long orderId) {
    List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);
    if (orderItems.isEmpty()) {
      return Collections.emptyList();
    }

    List<Long> productIds = orderItems.stream()
        .map(OrderItem::getProductId)
        .toList();
    List<Long> shippingAddressIds = orderItems.stream()
        .map(OrderItem::getUserAddressId)
        .toList();

    // Query list of products & shipping address from the orders
    List<Product> products = productRepository.findAllById(productIds);
    List<UserAddress> shippingAddress = userAddressRepository.findAllById(shippingAddressIds);

    Map<Long, Product> productMap = products.stream()
        .collect(Collectors.toMap(Product::getProductId, Function.identity()));
    Map<Long, UserAddress> userAddressMap = shippingAddress.stream()
        .collect(Collectors.toMap(UserAddress::getUserAddressId, Function.identity()));

    return orderItems.stream()
        .map(orderItem -> {
          Product product = productMap.get(orderItem.getProductId());
          UserAddress userAddress = userAddressMap.get(orderItem.getUserAddressId());

          if (product == null) {
            throw new ResourceNotFoundException(
                "Product with id " + orderItem.getProductId() + " is not found");
          }
          if (userAddress == null) {
            throw new ResourceNotFoundException(
                "User address with id " + orderItem.getUserAddressId() + " is not found");
          }

          return OrderItemResponse.fromOrderItemProductAndAddress(orderItem, product, userAddress);
        })
        .toList();
  }

  @Override
  public void updateOrderStatus(Long orderId, String newStatus) {
    Order order = orderRepository.findById(orderId)
        .orElseThrow(
            () -> new ResourceNotFoundException("Order with id " + orderId + " not found"));

    order.setStatus(newStatus);
    orderRepository.save(order);
  }

  @Override
  public Double calculateOrderTotal(Long orderId) {
    return orderItemRepository.calculateTotalOrder(orderId);
  }
}
