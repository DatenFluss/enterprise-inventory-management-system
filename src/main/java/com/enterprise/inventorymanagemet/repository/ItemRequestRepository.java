package com.enterprise.inventorymanagemet.repository;

import com.enterprise.inventorymanagemet.model.ItemRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRequestRepository extends JpaRepository<ItemRequest, Long> {
}
