package com.enterprise.inventorymanagement.service;

import com.enterprise.inventorymanagement.model.Department;
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
}
