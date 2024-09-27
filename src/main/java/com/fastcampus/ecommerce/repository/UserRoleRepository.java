package com.fastcampus.ecommerce.repository;

import com.fastcampus.ecommerce.entity.UserRole;
import com.fastcampus.ecommerce.entity.UserRole.UserRoleId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRoleRepository extends JpaRepository<UserRole, UserRoleId> {

  void deleteByUserId(Long userId);
}
