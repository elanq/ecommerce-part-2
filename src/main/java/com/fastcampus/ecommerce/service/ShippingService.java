package com.fastcampus.ecommerce.service;

import com.fastcampus.ecommerce.model.ShippingOrderRequest;
import com.fastcampus.ecommerce.model.ShippingOrderResponse;
import com.fastcampus.ecommerce.model.ShippingRateRequest;
import com.fastcampus.ecommerce.model.ShippingRateResponse;
import java.math.BigDecimal;

public interface ShippingService {

  ShippingRateResponse calculateShippingRate(ShippingRateRequest request);

  ShippingOrderResponse createShippingOrder(ShippingOrderRequest request);

  String generateAwbNumber(Long orderId);

  BigDecimal calculateTotalWeight(Long orderId);
}
