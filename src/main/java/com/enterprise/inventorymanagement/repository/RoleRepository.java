package com.enterprise.inventorymanagement.repository;

import com.enterprise.inventorymanagement.model.Role;
import com.enterprise.inventorymanagement.model.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, String> {
    Optional<Role> findByName(String name);
}
