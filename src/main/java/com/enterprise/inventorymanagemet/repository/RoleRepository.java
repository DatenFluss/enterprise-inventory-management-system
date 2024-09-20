package com.enterprise.inventorymanagemet.repository;

import com.enterprise.inventorymanagemet.model.Role;
import com.enterprise.inventorymanagemet.model.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, String> {
    Optional<Role> findByName(RoleName name);
}
