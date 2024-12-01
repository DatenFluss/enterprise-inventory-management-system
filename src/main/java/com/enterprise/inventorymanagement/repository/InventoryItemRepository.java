package com.enterprise.inventorymanagement.repository;

import com.enterprise.inventorymanagement.model.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {

    List<InventoryItem> findByQuantityGreaterThan(int quantity);

    Optional<InventoryItem> findByName(String name);

    List<InventoryItem> findByNameContainingIgnoreCase(String keyword);

    @Query("SELECT COUNT(i) > 0 FROM InventoryItem i WHERE i.name = :name AND i.enterprise.id = :enterpriseId")
    boolean existsByNameAndEnterpriseId(@Param("name") String name, @Param("enterpriseId") Long enterpriseId);

    @Query("SELECT i FROM InventoryItem i WHERE i.enterprise.id = :enterpriseId")
    List<InventoryItem> findAllByEnterpriseId(@Param("enterpriseId") Long enterpriseId);

    @Query("SELECT i FROM InventoryItem i WHERE i.warehouse.id = :warehouseId")
    List<InventoryItem> findAllByWarehouseId(@Param("warehouseId") Long warehouseId);

    @Query("SELECT i FROM InventoryItem i WHERE i.department.id = :departmentId")
    List<InventoryItem> findAllByDepartmentId(@Param("departmentId") Long departmentId);
}
