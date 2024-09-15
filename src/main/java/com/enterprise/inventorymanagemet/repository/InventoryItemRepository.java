package com.enterprise.inventorymanagemet.repository;

import com.enterprise.inventorymanagemet.model.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {
}

