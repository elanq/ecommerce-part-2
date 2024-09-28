package com.fastcampus.ecommerce.service;


import com.fastcampus.ecommerce.common.errors.InvalidPasswordException;
import com.fastcampus.ecommerce.model.AuthRequest;
import com.fastcampus.ecommerce.model.UserInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

  private final AuthenticationManager authenticationManager;

  @Override
  public UserInfo authenticate(AuthRequest authRequest) {
    try {
      Authentication authentication = authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(authRequest.getUsername(),
              authRequest.getPassword()));

      UserInfo user = (UserInfo) authentication.getPrincipal();
      return user;
    } catch (Exception ex) {
      log.error(ex.getMessage());
      throw new InvalidPasswordException("Invalid username or password");
    }
  }
}
