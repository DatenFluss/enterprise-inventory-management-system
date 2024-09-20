package com.enterprise.inventorymanagemet.repository;

import com.enterprise.inventorymanagemet.model.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {
    List<InventoryItem> findAllAvailableItems();
}
