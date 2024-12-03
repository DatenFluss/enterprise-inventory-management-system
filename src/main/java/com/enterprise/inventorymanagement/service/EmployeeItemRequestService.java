package com.enterprise.inventorymanagement.service;

import com.enterprise.inventorymanagement.model.dto.EmployeeItemRequestDTO;
import com.enterprise.inventorymanagement.model.request.RequestStatus;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface EmployeeItemRequestService {
    /**
     * Create a new item request from an employee
     * @param userId The ID of the employee creating the request
     * @param requestDTO The request details including items, quantities, and comments
     * @return The created request with all details
     */
    @Transactional
    EmployeeItemRequestDTO createRequest(Long userId, EmployeeItemRequestDTO requestDTO);

    /**
     * Handle (approve/reject) an employee's item request
     * @param requestId The ID of the request to handle
     * @param approved Whether the request is approved or rejected
     * @param responseComments Comments from the manager about the decision
     */
    @Transactional
    void handleRequest(Long requestId, boolean approved, String responseComments);

    /**
     * Get all requests for a specific department
     * @param departmentId The ID of the department
     * @return List of requests for the department
     */
    @Transactional(readOnly = true)
    List<EmployeeItemRequestDTO> getRequestsByDepartmentId(Long departmentId);

    /**
     * Get all requests for a specific department with a specific status
     * @param departmentId The ID of the department
     * @param status The status to filter by
     * @return List of filtered requests for the department
     */
    @Transactional(readOnly = true)
    List<EmployeeItemRequestDTO> getRequestsByDepartmentIdAndStatus(Long departmentId, RequestStatus status);

    /**
     * Get all requests made by a specific employee
     * @param userId The ID of the employee
     * @return List of requests made by the employee
     */
    @Transactional(readOnly = true)
    List<EmployeeItemRequestDTO> getRequestsByUserId(Long userId);

    /**
     * Get all requests made by a specific employee with a specific status
     * @param userId The ID of the employee
     * @param status The status to filter by
     * @return List of filtered requests made by the employee
     */
    @Transactional(readOnly = true)
    List<EmployeeItemRequestDTO> getRequestsByUserIdAndStatus(Long userId, RequestStatus status);
} 