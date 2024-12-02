package com.enterprise.inventorymanagement.repository;

import com.enterprise.inventorymanagement.model.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<InventoryItem, Long> {
    List<InventoryItem> findByWarehouse_Id(Long warehouseId);
    List<InventoryItem> findByDepartment_Id(Long departmentId);
    List<InventoryItem> findByEnterprise_Id(Long enterpriseId);
} 