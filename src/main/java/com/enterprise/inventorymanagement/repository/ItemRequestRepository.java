package com.enterprise.inventorymanagement.repository;

import com.enterprise.inventorymanagement.model.request.ItemRequest;
import com.enterprise.inventorymanagement.model.request.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ItemRequestRepository extends JpaRepository<ItemRequest, Long> {
    List<ItemRequest> findByRequesterId(Long requesterId);
    List<ItemRequest> findByRequesterIdAndStatus(Long requesterId, RequestStatus status);
    
    @Query("SELECT DISTINCT ir FROM ItemRequest ir JOIN ir.requestItems ri WHERE ri.inventoryItem.id = :itemId AND ir.status = :status")
    List<ItemRequest> findByInventoryItemIdAndStatus(@Param("itemId") Long itemId, @Param("status") RequestStatus status);
    
    @Query("SELECT ir FROM ItemRequest ir WHERE ir.sourceWarehouse.enterprise.id = :enterpriseId")
    List<ItemRequest> findByEnterpriseId(@Param("enterpriseId") Long enterpriseId);
    
    @Query("SELECT ir FROM ItemRequest ir WHERE ir.sourceWarehouse.id = :warehouseId")
    List<ItemRequest> findByWarehouseId(@Param("warehouseId") Long warehouseId);
    
    @Query("SELECT ir FROM ItemRequest ir WHERE ir.sourceWarehouse.id = :warehouseId AND ir.status = :status")
    List<ItemRequest> findByWarehouseIdAndStatus(@Param("warehouseId") Long warehouseId, @Param("status") RequestStatus status);
    
    @Query("SELECT ir FROM ItemRequest ir WHERE ir.targetDepartment.id = :departmentId")
    List<ItemRequest> findByDepartmentId(@Param("departmentId") Long departmentId);
    
    @Query("SELECT ir FROM ItemRequest ir WHERE ir.targetDepartment.id = :departmentId AND ir.status = :status")
    List<ItemRequest> findByDepartmentIdAndStatus(@Param("departmentId") Long departmentId, @Param("status") RequestStatus status);
    
    @Query("SELECT COUNT(ir) > 0 FROM ItemRequest ir JOIN ir.requestItems ri " +
           "WHERE ir.requester.id = :requesterId AND ri.inventoryItem.id = :itemId AND ir.status = :status")
    boolean existsByRequesterIdAndInventoryItemIdAndStatus(@Param("requesterId") Long requesterId, 
                                                         @Param("itemId") Long itemId, 
                                                         @Param("status") RequestStatus status);
}
