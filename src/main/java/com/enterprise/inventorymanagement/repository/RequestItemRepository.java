package com.enterprise.inventorymanagement.repository;

import com.enterprise.inventorymanagement.model.request.RequestItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RequestItemRepository extends JpaRepository<RequestItem, Long> {
    List<RequestItem> findByRequestId(Long requestId);
    List<RequestItem> findByInventoryItemId(Long itemId);
} 