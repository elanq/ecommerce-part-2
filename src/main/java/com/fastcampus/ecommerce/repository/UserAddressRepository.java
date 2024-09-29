package com.fastcampus.ecommerce.repository;

import com.fastcampus.ecommerce.entity.UserAddress;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserAddressRepository extends JpaRepository<UserAddress, Long> {

  List<UserAddress> findByUserId(Long userId);

  Optional<UserAddress> findByUserIdAndIsDefaultTrue(Long userId);

  @Query(value = """
      UPDATE user_addresses SET is_default = false
      WHERE user_id = :userId
      """, nativeQuery = true)
  @Modifying
  void resetUserDefaultAddress(Long userId);

  @Query(value = """
      UPDATE user_addresses SET is_default = true
      WHERE user_address_id = :addressId
      """, nativeQuery = true)
  @Modifying
  void setDefaultAddress(Long addressId);

}
