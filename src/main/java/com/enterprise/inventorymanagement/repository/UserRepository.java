package com.enterprise.inventorymanagement.repository;

import com.enterprise.inventorymanagement.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.department WHERE u.id = :id")
    Optional<User> findByIdWithDepartment(@Param("id") Long id);
    @Query("SELECT u FROM User u WHERE u.manager.id = :managerId")
    List<User> findByManagerId(@Param("managerId") Long managerId);
    @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.enterprise LEFT JOIN FETCH u.department WHERE u.enterprise.id = :enterpriseId")
    List<User> findByEnterpriseId(@Param("enterpriseId") Long enterpriseId);
    Optional<User> findByEmailAndActive(String email, boolean active);

    @Query("SELECT DISTINCT u FROM User u " +
           "LEFT JOIN FETCH u.department " +
           "LEFT JOIN FETCH u.role " +
           "WHERE u.role.name = 'ROLE_EMPLOYEE' " +
           "AND u.department IS NULL " +
           "AND u.enterprise.id = :enterpriseId")
    List<User> findAvailableEmployees(@Param("enterpriseId") Long enterpriseId);
}
