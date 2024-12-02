package com.enterprise.inventorymanagement.repository;

import com.enterprise.inventorymanagement.model.request.ItemRequest;
import com.enterprise.inventorymanagement.model.request.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemRequestRepository extends JpaRepository<ItemRequest, Long> {
    List<ItemRequest> findBySourceWarehouseId(Long warehouseId);
    List<ItemRequest> findBySourceWarehouseIdAndStatus(Long warehouseId, RequestStatus status);
    List<ItemRequest> findByTargetDepartmentId(Long departmentId);
    List<ItemRequest> findByTargetDepartmentIdAndStatus(Long departmentId, RequestStatus status);
    List<ItemRequest> findByRequesterId(Long requesterId);
    List<ItemRequest> findByRequesterIdAndStatus(Long requesterId, RequestStatus status);
}
