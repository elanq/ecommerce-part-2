package com.fastcampus.ecommerce.service;

import com.fastcampus.ecommerce.model.UserInfo;

public interface JwtService {

  String generateToken(UserInfo userInfo);

  boolean validateToken(String token);

  String getUsernameFromToken(String token);
}
