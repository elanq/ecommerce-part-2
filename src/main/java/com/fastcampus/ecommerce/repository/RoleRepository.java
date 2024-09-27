package com.fastcampus.ecommerce.repository;

import com.fastcampus.ecommerce.entity.Role;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RoleRepository extends JpaRepository<Role, Long> {

  Optional<Role> findByName(String name);

  @Query(value = """
      SELECT r.* FROM roles r
      JOIN user_role ur ON ur.role_id = r.role_id
      JOIN users u ON ur.user_id = u.user_id
      WHERE u.user_id = :userId
      """, nativeQuery = true)
  List<Role> findByUserId(@Param("userId") Long userId);
}
