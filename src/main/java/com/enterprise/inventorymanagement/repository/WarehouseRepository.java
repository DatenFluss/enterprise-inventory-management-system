package com.enterprise.inventorymanagement.repository;

import com.enterprise.inventorymanagement.model.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {
    List<Warehouse> findByEnterpriseId(Long enterpriseId);
    Optional<Warehouse> findByIdAndEnterpriseId(Long id, Long enterpriseId);
    boolean existsByNameAndEnterpriseId(String name, Long enterpriseId);
    List<Warehouse> findByManagerId(Long managerId);
}
