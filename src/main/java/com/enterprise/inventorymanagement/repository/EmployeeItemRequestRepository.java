package com.enterprise.inventorymanagement.repository;

import com.enterprise.inventorymanagement.model.request.EmployeeItemRequest;
import com.enterprise.inventorymanagement.model.request.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeItemRequestRepository extends JpaRepository<EmployeeItemRequest, Long> {
    
    @Query("SELECT DISTINCT r FROM EmployeeItemRequest r " +
           "LEFT JOIN FETCH r.requestItems ri " +
           "LEFT JOIN FETCH ri.inventoryItem " +
           "LEFT JOIN FETCH r.requester " +
           "LEFT JOIN FETCH r.department " +
           "LEFT JOIN FETCH r.processor " +
           "WHERE r.id = :id")
    Optional<EmployeeItemRequest> findByIdWithItems(@Param("id") Long id);

    @Query("SELECT DISTINCT r FROM EmployeeItemRequest r " +
           "LEFT JOIN FETCH r.requestItems ri " +
           "LEFT JOIN FETCH ri.inventoryItem " +
           "LEFT JOIN FETCH r.requester " +
           "LEFT JOIN FETCH r.department " +
           "LEFT JOIN FETCH r.processor " +
           "WHERE r.department.id = :departmentId")
    List<EmployeeItemRequest> findByDepartmentId(@Param("departmentId") Long departmentId);

    @Query("SELECT DISTINCT r FROM EmployeeItemRequest r " +
           "LEFT JOIN FETCH r.requestItems ri " +
           "LEFT JOIN FETCH ri.inventoryItem " +
           "LEFT JOIN FETCH r.requester " +
           "LEFT JOIN FETCH r.department " +
           "LEFT JOIN FETCH r.processor " +
           "WHERE r.department.id = :departmentId AND r.status = :status")
    List<EmployeeItemRequest> findByDepartmentIdAndStatus(
            @Param("departmentId") Long departmentId,
            @Param("status") RequestStatus status);

    @Query("SELECT DISTINCT r FROM EmployeeItemRequest r " +
           "LEFT JOIN FETCH r.requestItems ri " +
           "LEFT JOIN FETCH ri.inventoryItem " +
           "LEFT JOIN FETCH r.requester " +
           "LEFT JOIN FETCH r.department " +
           "LEFT JOIN FETCH r.processor " +
           "WHERE r.requester.id = :requesterId")
    List<EmployeeItemRequest> findByRequesterId(@Param("requesterId") Long requesterId);

    @Query("SELECT DISTINCT r FROM EmployeeItemRequest r " +
           "LEFT JOIN FETCH r.requestItems ri " +
           "LEFT JOIN FETCH ri.inventoryItem " +
           "LEFT JOIN FETCH r.requester " +
           "LEFT JOIN FETCH r.department " +
           "LEFT JOIN FETCH r.processor " +
           "WHERE r.requester.id = :requesterId AND r.status = :status")
    List<EmployeeItemRequest> findByRequesterIdAndStatus(
            @Param("requesterId") Long requesterId,
            @Param("status") RequestStatus status);
} 