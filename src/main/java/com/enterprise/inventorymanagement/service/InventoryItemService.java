package com.enterprise.inventorymanagement.service;

import com.enterprise.inventorymanagement.model.dto.ItemRequestDTO;
import jakarta.transaction.Transactional;

import java.util.List;

public interface InventoryItemService {
    /**
     * Create a new item request from a department to a warehouse
     * @param userId The ID of the user (department manager) creating the request
     * @param requestDTO The request details including items, quantities, and comments
     * @return The created request with all details
     */
    @Transactional
    ItemRequestDTO createItemRequest(Long userId, ItemRequestDTO requestDTO);

    /**
     * Handle (approve/reject) an item request
     * @param requestId The ID of the request to handle
     * @param approved Whether the request is approved or rejected
     * @param responseComments Comments from the warehouse operator about the decision
     */
    @Transactional
    void handleItemRequest(Long requestId, boolean approved, String responseComments);

    /**
     * Get all requests for a specific warehouse
     * @param warehouseId The ID of the warehouse
     * @return List of requests for the warehouse
     */
    @Transactional
    List<ItemRequestDTO> getRequestsByWarehouseId(Long warehouseId);

    /**
     * Get all requests for a specific department
     * @param departmentId The ID of the department
     * @return List of requests for the department
     */
    @Transactional
    List<ItemRequestDTO> getRequestsByDepartmentId(Long departmentId);
}


