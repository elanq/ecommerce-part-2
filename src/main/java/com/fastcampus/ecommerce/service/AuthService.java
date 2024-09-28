package com.fastcampus.ecommerce.service;

import com.fastcampus.ecommerce.model.AuthRequest;
import com.fastcampus.ecommerce.model.UserInfo;

public interface AuthService {

  UserInfo authenticate(AuthRequest authRequest);
}
