package com.fastcampus.ecommerce.model;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequest {
  @NotBlank(message = "Nama produk tidak boleh kosong")
  @Size(min = 3, max = 20, message = "Nama produk harus antara 30 dan 20 karakter")
  String name;

  @NotNull(message = "Harga tidak boleh kosong")
  @Positive(message = "Harga harus lebih besar dari 0")
  @Digits(integer = 10, fraction = 2, message = "Harga harus memiliki maksimal 10 digit dan 2 angka di belakang koma")
  BigDecimal price;

  @NotNull(message = "Deskripsi produk tidak boleh null")
  @Size(max = 1000, message = "Deskripsi produk tidak boleh lebih dari 1000 karakter")
  String description;
}
