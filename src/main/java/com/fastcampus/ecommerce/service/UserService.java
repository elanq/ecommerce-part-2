package com.fastcampus.ecommerce.service;

import com.fastcampus.ecommerce.model.UserRegisterRequest;
import com.fastcampus.ecommerce.model.UserResponse;
import com.fastcampus.ecommerce.model.UserUpdateRequest;

public interface UserService {
  UserResponse register(UserRegisterRequest registerRequest);
  UserResponse findById(Long id);
  UserResponse findByKeyword(String keyword);
  UserResponse updateUser(Long id, UserUpdateRequest request);
  void deleteUser(Long id);
  boolean existsByUsername(String username);
  boolean existsByEmail(String email);
}
