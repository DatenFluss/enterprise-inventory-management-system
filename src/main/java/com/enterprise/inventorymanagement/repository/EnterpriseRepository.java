package com.enterprise.inventorymanagement.repository;

import com.enterprise.inventorymanagement.model.Enterprise;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EnterpriseRepository extends JpaRepository<Enterprise, Long> {

    @NotNull Optional<Enterprise> findById(@NotNull Long id);

    @Query("SELECT DISTINCT e FROM Enterprise e " +
           "LEFT JOIN FETCH e.employees emp " +
           "LEFT JOIN FETCH emp.role " +
           "LEFT JOIN FETCH emp.department " +
           "LEFT JOIN FETCH e.departments d " +
           "WHERE e.id = :id")
    Optional<Enterprise> findByIdWithEmployees(@Param("id") Long id);

    @Query("SELECT DISTINCT e FROM Enterprise e " +
           "LEFT JOIN FETCH e.employees emp " +
           "LEFT JOIN FETCH emp.role " +
           "LEFT JOIN FETCH emp.department " +
           "LEFT JOIN FETCH e.departments")
    List<Enterprise> findAllWithEmployees();
}
