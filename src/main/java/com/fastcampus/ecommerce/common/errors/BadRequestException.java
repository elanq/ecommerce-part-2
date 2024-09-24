package com.fastcampus.ecommerce.common.errors;

public class BadRequestException extends RuntimeException {
  public BadRequestException(String message) {
    super(message);
  }
}