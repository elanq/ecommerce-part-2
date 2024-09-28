package com.fastcampus.ecommerce.common.errors;

public class EmailAlreadyExistsException extends RuntimeException{
  public EmailAlreadyExistsException(String message) {
    super(message);
  }
}
