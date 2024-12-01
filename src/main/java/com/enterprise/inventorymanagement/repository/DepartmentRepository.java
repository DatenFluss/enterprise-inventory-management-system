package com.enterprise.inventorymanagement.repository;

import com.enterprise.inventorymanagement.model.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
    @Query("SELECT d FROM Department d WHERE d.enterprise.id = :enterpriseId")
    List<Department> findByEnterpriseId(@Param("enterpriseId") Long enterpriseId);
    
    @Query("SELECT d FROM Department d WHERE d.id = :id AND d.enterprise.id = :enterpriseId")
    Optional<Department> findByIdAndEnterpriseId(@Param("id") Long id, @Param("enterpriseId") Long enterpriseId);
    
    @Query("SELECT COUNT(d) > 0 FROM Department d WHERE d.name = :name AND d.enterprise.id = :enterpriseId")
    boolean existsByNameAndEnterpriseId(@Param("name") String name, @Param("enterpriseId") Long enterpriseId);
    
    @Query("SELECT d FROM Department d WHERE d.manager.id = :managerId")
    List<Department> findByManagerId(@Param("managerId") Long managerId);
}
