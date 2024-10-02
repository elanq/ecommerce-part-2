package com.fastcampus.ecommerce.service;


import com.fastcampus.ecommerce.common.errors.BadRequestException;
import com.fastcampus.ecommerce.common.errors.ForbiddenAccessException;
import com.fastcampus.ecommerce.common.errors.InventoryException;
import com.fastcampus.ecommerce.common.errors.ResourceNotFoundException;
import com.fastcampus.ecommerce.entity.Cart;
import com.fastcampus.ecommerce.entity.CartItem;
import com.fastcampus.ecommerce.entity.Product;
import com.fastcampus.ecommerce.model.CartItemResponse;
import com.fastcampus.ecommerce.repository.CartItemRepository;
import com.fastcampus.ecommerce.repository.CartRepository;
import com.fastcampus.ecommerce.repository.ProductRepository;
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
public class CartServiceImpl implements CartService {

  private final CartRepository cartRepository;
  private final CartItemRepository cartItemRepository;
  private final ProductRepository productRepository;

  @Override
  @Transactional
  public void addItemToCart(Long userId, Long productId, int quantity) {
    Cart cart = cartRepository.findByUserId(userId)
        .orElseGet(() -> {
          Cart newCart = Cart.builder()
              .userId(userId)
              .build();
          return cartRepository.save(newCart);
        });

    Product product = productRepository.findByIdWithPessimisticLock(productId)
        .orElseThrow(
            () -> new ResourceNotFoundException("product with id " + productId + " is not found"));

    if (product.getUserId().equals(userId)) {
      throw new BadRequestException("Cannot add your own product to cart");
    }

    if (product.getStockQuantity() <= 0) {
      throw new InventoryException("Product stock is equal or below zero");
    }

    Optional<CartItem> existingItemOpt = cartItemRepository.findByCartIdAndProductId(
        cart.getCartId(), productId);

    if (existingItemOpt.isPresent()) {
      CartItem existingItem = existingItemOpt.get();
      existingItem.setQuantity(existingItem.getQuantity() + quantity);
      cartItemRepository.save(existingItem);
    } else {
      CartItem newItem = CartItem.builder()
          .cartId(cart.getCartId())
          .productId(productId)
          .quantity(quantity)
          .price(product.getPrice())
          .build();
      cartItemRepository.save(newItem);
    }
  }

  @Transactional
  @Override
  public void updateCartItemQuantity(Long userId, Long productId, int quantity) {
    Cart cart = cartRepository.findByUserId(userId)
        .orElseThrow(
            () -> new ResourceNotFoundException("Cart not found for user with id " + userId));

    Optional<CartItem> existingItemOpt = cartItemRepository.findByCartIdAndProductId(
        cart.getCartId(), productId);

    if (existingItemOpt.isEmpty()) {
      throw new ResourceNotFoundException("Product " + productId + " is not yet added to cart");
    }

    CartItem item = existingItemOpt.get();
    if (!item.getCartId().equals(cart.getCartId())) {
      throw new ForbiddenAccessException("Cart item doesn't belong to user's cart");
    }

    if (quantity <= 0) {
      cartItemRepository.deleteById(item.getCartItemId());
    } else {
      item.setQuantity(quantity);
      cartItemRepository.save(item);
    }
  }


  @Transactional
  @Override
  public void removeItemFromCart(Long userId, Long cartItemId) {
    Cart cart = cartRepository.findByUserId(userId)
        .orElseThrow(
            () -> new ResourceNotFoundException("Cart not found for user with id " + userId));

    Optional<CartItem> existingItemOpt = cartItemRepository.findById(cartItemId);

    if (existingItemOpt.isEmpty()) {
      throw new ResourceNotFoundException("Cart item not found");
    }

    CartItem item = existingItemOpt.get();

    if (!item.getCartId().equals(cart.getCartId())) {
      throw new ForbiddenAccessException("Cart item doesn't belong to user's cart");
    }

    cartItemRepository.deleteById(cartItemId);
  }

  @Transactional
  @Override
  public void clearCart(Long userId) {
    Cart cart = cartRepository.findByUserId(userId)
        .orElseThrow(
            () -> new ResourceNotFoundException("Cart not found for user with id " + userId));
    cartItemRepository.deleteAllByCartId(cart.getCartId());
  }

  @Override
  public List<CartItemResponse> getCartItems(Long userId) {
    List<CartItem> cartItems = cartItemRepository.getUserCartItems(userId);
    if (cartItems.isEmpty()) {
      return Collections.emptyList();
    }

    List<Long> productIds = cartItems.stream()
        .map(CartItem::getProductId)
        .toList();

    List<Product> products = productRepository.findAllById(productIds);

    Map<Long, Product> productMap = products.stream()
        .collect(Collectors.toMap(Product::getProductId, Function.identity()));

    return cartItems.stream()
        .map(cartItem -> {
          Product product = productMap.get(cartItem.getProductId());
          if (product == null) {
            throw new ResourceNotFoundException(
                "Product not found for ID: " + cartItem.getProductId());
          }
          return CartItemResponse.fromCartItemAndProduct(cartItem, product);
        })
        .toList();
  }
}
