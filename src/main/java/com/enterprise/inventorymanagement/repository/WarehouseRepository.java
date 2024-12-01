package com.enterprise.inventorymanagement.repository;

import com.enterprise.inventorymanagement.model.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {
    @Query("SELECT w FROM Warehouse w WHERE w.enterprise.id = :enterpriseId")
    List<Warehouse> findByEnterpriseId(@Param("enterpriseId") Long enterpriseId);
    
    @Query("SELECT w FROM Warehouse w WHERE w.id = :id AND w.enterprise.id = :enterpriseId")
    Optional<Warehouse> findByIdAndEnterpriseId(@Param("id") Long id, @Param("enterpriseId") Long enterpriseId);
    
    @Query("SELECT COUNT(w) > 0 FROM Warehouse w WHERE w.name = :name AND w.enterprise.id = :enterpriseId")
    boolean existsByNameAndEnterpriseId(@Param("name") String name, @Param("enterpriseId") Long enterpriseId);
    
    @Query("SELECT w FROM Warehouse w WHERE w.manager.id = :managerId")
    List<Warehouse> findByManagerId(@Param("managerId") Long managerId);
}
