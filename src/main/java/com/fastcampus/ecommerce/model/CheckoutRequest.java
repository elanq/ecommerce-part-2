package com.fastcampus.ecommerce.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(SnakeCaseStrategy.class)
public class CheckoutRequest {

  private Long userId;

  @NotEmpty(message = "At least one cart item must be selected for checkout")
  @Size(min = 1, message = "At least one cart item must be selected")
  private List<Long> selectedCartItemIds;

  @NotNull(message = "User Address ID is required")
  private Long userAddressId;
}
