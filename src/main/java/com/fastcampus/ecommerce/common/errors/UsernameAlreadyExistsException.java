package com.fastcampus.ecommerce.common.errors;

public class UsernameAlreadyExistsException extends RuntimeException{
  public UsernameAlreadyExistsException(String message) {
    super(message);
  }
}
