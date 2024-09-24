package com.fastcampus.ecommerce.controller;

import com.fastcampus.ecommerce.common.errors.BadRequestException;
import com.fastcampus.ecommerce.common.errors.ResourceNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

  // localhost:8080/hello
  @GetMapping("/hello")
  public String hello() {
    return "Hello world";
  }

  @GetMapping("/bad-request")
  public ResponseEntity<String> badRequestError() {
    throw new BadRequestException("ada error bad request");
  }

  @GetMapping("/generic-error")
  public ResponseEntity<String> genericError() {
    throw new RuntimeException("generic error");
  }

  @GetMapping("/not-found")
  public ResponseEntity<String> notFoundError() {
    throw new ResourceNotFoundException("data tidak ditemukan");
  }
}
