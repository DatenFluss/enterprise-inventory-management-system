package com.enterprise.inventorymanagement.repository;

import com.enterprise.inventorymanagement.model.DepartmentInvite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DepartmentInviteRepository extends JpaRepository<DepartmentInvite, Long> {
    List<DepartmentInvite> findByDepartmentIdAndStatus(Long departmentId, String status);
    List<DepartmentInvite> findByUserIdAndStatus(Long userId, String status);
    boolean existsByUserIdAndDepartmentIdAndStatus(Long userId, Long departmentId, String status);
} 