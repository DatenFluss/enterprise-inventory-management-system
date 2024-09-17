package com.enterprise.inventorymanagemet.repository;

import com.enterprise.inventorymanagemet.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, String> {
    Role findByName(String name);
}
