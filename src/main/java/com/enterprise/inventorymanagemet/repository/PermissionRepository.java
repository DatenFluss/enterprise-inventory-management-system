package com.enterprise.inventorymanagemet.repository;

import com.enterprise.inventorymanagemet.model.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissionRepository extends JpaRepository<Permission, Long> {
}
