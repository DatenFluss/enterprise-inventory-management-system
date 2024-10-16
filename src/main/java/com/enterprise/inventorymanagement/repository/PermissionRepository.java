package com.enterprise.inventorymanagement.repository;

import com.enterprise.inventorymanagement.model.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissionRepository extends JpaRepository<Permission, Long> {
}
