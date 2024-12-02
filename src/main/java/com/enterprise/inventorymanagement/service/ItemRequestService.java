package com.enterprise.inventorymanagement.service;

import com.enterprise.inventorymanagement.model.dto.ItemRequestDTO;
import com.enterprise.inventorymanagement.model.request.RequestStatus;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ItemRequestService {
    /**
     * Create a new item request
     * @param userId The ID of the user creating the request
     * @param requestDTO The request details
     * @return The created request
     */
    @Transactional
    ItemRequestDTO createItemRequest(Long userId, ItemRequestDTO requestDTO);

    /**
     * Handle (approve/reject) an item request
     * @param requestId The ID of the request to handle
     * @param approved Whether to approve or reject the request
     * @param responseComments Comments from the processor
     */
    @Transactional
    void handleItemRequest(Long requestId, boolean approved, String responseComments);

    /**
     * Get all requests for a specific warehouse
     * @param warehouseId The ID of the warehouse
     * @return List of requests for the warehouse
     */
    @Transactional(readOnly = true)
    List<ItemRequestDTO> getRequestsByWarehouseId(Long warehouseId);

    /**
     * Get all requests for a specific warehouse with a specific status
     * @param warehouseId The ID of the warehouse
     * @param status The status to filter by
     * @return List of filtered requests for the warehouse
     */
    @Transactional(readOnly = true)
    List<ItemRequestDTO> getRequestsByWarehouseIdAndStatus(Long warehouseId, RequestStatus status);

    /**
     * Get all requests for a specific department
     * @param departmentId The ID of the department
     * @return List of requests for the department
     */
    @Transactional(readOnly = true)
    List<ItemRequestDTO> getRequestsByDepartmentId(Long departmentId);

    /**
     * Get all requests for a specific department with a specific status
     * @param departmentId The ID of the department
     * @param status The status to filter by
     * @return List of filtered requests for the department
     */
    @Transactional(readOnly = true)
    List<ItemRequestDTO> getRequestsByDepartmentIdAndStatus(Long departmentId, RequestStatus status);

    /**
     * Get all requests for a specific user
     * @param userId The ID of the user
     * @return List of requests for the user
     */
    @Transactional(readOnly = true)
    List<ItemRequestDTO> getRequestsByUserId(Long userId);

    /**
     * Get all requests for a specific user with a specific status
     * @param userId The ID of the user
     * @param status The status to filter by
     * @return List of filtered requests for the user
     */
    @Transactional(readOnly = true)
    List<ItemRequestDTO> getRequestsByUserIdAndStatus(Long userId, RequestStatus status);
}

