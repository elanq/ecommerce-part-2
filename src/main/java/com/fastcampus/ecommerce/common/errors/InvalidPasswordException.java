package com.fastcampus.ecommerce.common.errors;

public class InvalidPasswordException extends RuntimeException{
  public InvalidPasswordException(String message) {
    super(message);
  }
}
