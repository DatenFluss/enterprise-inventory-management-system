package com.enterprise.inventorymanagement.repository;

import com.enterprise.inventorymanagement.model.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
    List<Department> findByEnterpriseId(Long enterpriseId);
    Optional<Department> findByIdAndEnterpriseId(Long id, Long enterpriseId);
    boolean existsByNameAndEnterpriseId(String name, Long enterpriseId);
    List<Department> findByManagerId(Long managerId);
}
