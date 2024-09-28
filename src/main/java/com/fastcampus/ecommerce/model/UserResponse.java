package com.fastcampus.ecommerce.model;

import com.fastcampus.ecommerce.entity.Role;
import com.fastcampus.ecommerce.entity.User;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class UserResponse implements Serializable {
  private Long userId;
  private String username;
  private String email;
  private boolean enabled;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private List<String> roles;

  public static UserResponse fromUserAndRoles(User user, List<Role> roles) {
    return UserResponse.builder()
        .userId(user.getUserId())
        .username(user.getUsername())
        .email(user.getEmail())
        .enabled(user.isEnabled())
        .createdAt(user.getCreatedAt())
        .updatedAt(user.getUpdatedAt())
        .roles(roles.stream().map(Role::getName).toList())
        .build();
  }
}
