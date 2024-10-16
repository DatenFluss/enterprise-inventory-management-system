package com.enterprise.inventorymanagement.repository;

import com.enterprise.inventorymanagement.model.request.ItemRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRequestRepository extends JpaRepository<ItemRequest, Long> {
}
