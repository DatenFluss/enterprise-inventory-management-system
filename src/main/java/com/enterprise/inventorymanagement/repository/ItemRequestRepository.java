package com.enterprise.inventorymanagement.repository;

import com.enterprise.inventorymanagement.model.request.ItemRequest;
import com.enterprise.inventorymanagement.model.request.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemRequestRepository extends JpaRepository<ItemRequest, Long> {
    @Query("SELECT DISTINCT r FROM ItemRequest r " +
           "LEFT JOIN FETCH r.requestItems ri " +
           "LEFT JOIN FETCH ri.inventoryItem " +
           "LEFT JOIN FETCH r.requester " +
           "LEFT JOIN FETCH r.sourceWarehouse " +
           "LEFT JOIN FETCH r.targetDepartment " +
           "WHERE r.sourceWarehouse.id = :warehouseId")
    List<ItemRequest> findBySourceWarehouseId(@Param("warehouseId") Long warehouseId);

    @Query("SELECT DISTINCT r FROM ItemRequest r " +
           "LEFT JOIN FETCH r.requestItems ri " +
           "LEFT JOIN FETCH ri.inventoryItem " +
           "LEFT JOIN FETCH r.requester " +
           "LEFT JOIN FETCH r.sourceWarehouse " +
           "LEFT JOIN FETCH r.targetDepartment " +
           "WHERE r.sourceWarehouse.id = :warehouseId AND r.status = :status")
    List<ItemRequest> findBySourceWarehouseIdAndStatus(@Param("warehouseId") Long warehouseId, @Param("status") RequestStatus status);

    @Query("SELECT DISTINCT r FROM ItemRequest r " +
           "LEFT JOIN FETCH r.requestItems ri " +
           "LEFT JOIN FETCH ri.inventoryItem " +
           "LEFT JOIN FETCH r.requester " +
           "LEFT JOIN FETCH r.sourceWarehouse " +
           "LEFT JOIN FETCH r.targetDepartment " +
           "WHERE r.id = :id")
    Optional<ItemRequest> findByIdWithItems(@Param("id") Long id);

    @Query("SELECT DISTINCT r FROM ItemRequest r " +
           "LEFT JOIN FETCH r.requestItems ri " +
           "LEFT JOIN FETCH ri.inventoryItem " +
           "LEFT JOIN FETCH r.requester " +
           "LEFT JOIN FETCH r.sourceWarehouse " +
           "LEFT JOIN FETCH r.targetDepartment " +
           "WHERE r.requester.id = :userId")
    List<ItemRequest> findByRequesterId(@Param("userId") Long userId);

    @Query("SELECT DISTINCT r FROM ItemRequest r " +
           "LEFT JOIN FETCH r.requestItems ri " +
           "LEFT JOIN FETCH ri.inventoryItem " +
           "LEFT JOIN FETCH r.requester " +
           "LEFT JOIN FETCH r.sourceWarehouse " +
           "LEFT JOIN FETCH r.targetDepartment " +
           "WHERE r.requester.id = :userId AND r.status = :status")
    List<ItemRequest> findByRequesterIdAndStatus(@Param("userId") Long userId, @Param("status") RequestStatus status);

    @Query("SELECT DISTINCT r FROM ItemRequest r " +
           "LEFT JOIN FETCH r.requestItems ri " +
           "LEFT JOIN FETCH ri.inventoryItem " +
           "LEFT JOIN FETCH r.requester " +
           "LEFT JOIN FETCH r.sourceWarehouse " +
           "LEFT JOIN FETCH r.targetDepartment " +
           "WHERE r.targetDepartment.id = :departmentId")
    List<ItemRequest> findByTargetDepartmentId(@Param("departmentId") Long departmentId);

    @Query("SELECT DISTINCT r FROM ItemRequest r " +
           "LEFT JOIN FETCH r.requestItems ri " +
           "LEFT JOIN FETCH ri.inventoryItem " +
           "LEFT JOIN FETCH r.requester " +
           "LEFT JOIN FETCH r.sourceWarehouse " +
           "LEFT JOIN FETCH r.targetDepartment " +
           "WHERE r.targetDepartment.id = :departmentId AND r.status = :status")
    List<ItemRequest> findByTargetDepartmentIdAndStatus(@Param("departmentId") Long departmentId, @Param("status") RequestStatus status);
}
