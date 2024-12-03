package com.enterprise.inventorymanagement.service;

import com.enterprise.inventorymanagement.exceptions.ResourceNotFoundException;
import com.enterprise.inventorymanagement.model.Department;
import com.enterprise.inventorymanagement.model.User;
import com.enterprise.inventorymanagement.model.dto.DepartmentDTO;
import java.util.List;

public interface DepartmentService {

    /**
     * Create a new department in an enterprise
     */
    Department createDepartment(Long enterpriseId, Department department);

    /**
     * Update an existing department
     */
    Department updateDepartment(Long departmentId, Department departmentDetails);

    /**
     * Delete a department
     */
    void deleteDepartment(Long departmentId);

    /**
     * Assign a manager to a department
     */
    Department assignManager(Long departmentId, Long userId);

    /**
     * Get all departments for an enterprise
     */
    List<DepartmentDTO> getDepartmentsByEnterpriseId(Long enterpriseId);

    /**
     * Get a department by its ID
     */
    Department getDepartmentById(Long departmentId) throws ResourceNotFoundException;

    /**
     * Get all employees in a department with complete user data
     */
    List<User> getDepartmentEmployeesWithData(Long departmentId) throws ResourceNotFoundException;

    void addEmployeeToDepartment(Long departmentId, Long userId, Long enterpriseId) throws ResourceNotFoundException, IllegalArgumentException;
}
