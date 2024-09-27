package com.enterprise.inventorymanagemet.repository;

import com.enterprise.inventorymanagemet.model.request.ItemRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRequestRepository extends JpaRepository<ItemRequest, Long> {
}
