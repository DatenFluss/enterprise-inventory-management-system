package com.enterprise.inventorymanagement.repository;

import com.enterprise.inventorymanagement.model.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {

    List<InventoryItem> findByQuantityGreaterThan(int quantity);

    Optional<InventoryItem> findByName(String name);

    List<InventoryItem> findByNameContainingIgnoreCase(String keyword);

    @Query("SELECT DISTINCT i FROM InventoryItem i " +
           "LEFT JOIN FETCH i.enterprise e " +
           "LEFT JOIN FETCH i.warehouse w " +
           "LEFT JOIN FETCH w.enterprise we " +
           "LEFT JOIN FETCH i.department d " +
           "LEFT JOIN FETCH d.enterprise de " +
           "WHERE i.id = :id")
    Optional<InventoryItem> findByIdWithRelationships(@Param("id") Long id);

    @Query("SELECT COUNT(i) > 0 FROM InventoryItem i WHERE i.name = :name AND i.enterprise.id = :enterpriseId")
    boolean existsByNameAndEnterpriseId(@Param("name") String name, @Param("enterpriseId") Long enterpriseId);

    @Query("SELECT i FROM InventoryItem i WHERE i.enterprise.id = :enterpriseId")
    List<InventoryItem> findAllByEnterpriseId(@Param("enterpriseId") Long enterpriseId);

    @Query("SELECT i FROM InventoryItem i WHERE i.warehouse.id = :warehouseId AND i.department IS NULL")
    List<InventoryItem> findAllByWarehouseId(@Param("warehouseId") Long warehouseId);

    @Query("SELECT SUM(i.quantity) FROM InventoryItem i WHERE i.warehouse.id = :warehouseId")
    Integer countItemsByWarehouseId(@Param("warehouseId") Long warehouseId);

    @Query("SELECT i FROM InventoryItem i WHERE i.department.id = :departmentId AND i.user IS NULL")
    List<InventoryItem> findAllByDepartmentId(@Param("departmentId") Long departmentId);

    List<InventoryItem> findAllByUserId(Long userId);

    List<InventoryItem> findByDepartment_Id(Long departmentId);

    @Query("SELECT i FROM InventoryItem i " +
           "LEFT JOIN FETCH i.department d " +
           "LEFT JOIN FETCH i.enterprise e " +
           "WHERE i.name = :name AND i.department.id = :departmentId AND i.user.id = :userId")
    Optional<InventoryItem> findByNameAndDepartment_IdAndUserId(
            @Param("name") String name,
            @Param("departmentId") Long departmentId,
            @Param("userId") Long userId);

    @Query("SELECT i FROM InventoryItem i WHERE i.warehouse.id = :warehouseId AND i.department IS NULL")
    List<InventoryItem> findByWarehouse_Id(@Param("warehouseId") Long warehouseId);

    @Query("SELECT i FROM InventoryItem i WHERE i.name = :name AND i.warehouse.id = :warehouseId AND i.department IS NULL")
    Optional<InventoryItem> findByNameAndWarehouse_Id(@Param("name") String name, @Param("warehouseId") Long warehouseId);

    @Query("SELECT i FROM InventoryItem i WHERE i.name = :name AND i.department.id = :departmentId AND i.user IS NULL")
    Optional<InventoryItem> findByNameAndDepartment_Id(@Param("name") String name, @Param("departmentId") Long departmentId);
}
