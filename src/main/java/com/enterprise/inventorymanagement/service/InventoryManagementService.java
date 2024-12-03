package com.enterprise.inventorymanagement.service;

import com.enterprise.inventorymanagement.model.InventoryItem;
import com.enterprise.inventorymanagement.model.dto.ItemDTO;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;

public interface InventoryManagementService {
    /**
     * Save a new inventory item
     */
    @Transactional
    ItemDTO createItem(Long enterpriseId, ItemDTO itemDTO);

    /**
     * Update an existing inventory item
     */
    @Transactional
    ItemDTO updateItem(Long id, ItemDTO itemDTO);

    /**
     * Delete an inventory item
     */
    @Transactional
    void deleteItem(Long id);

    /**
     * Get an item by its ID
     */
    Optional<ItemDTO> getItemById(Long id);

    /**
     * Get all items in an enterprise
     */
    List<ItemDTO> getAllItemsByEnterpriseId(Long enterpriseId);

    /**
     * Get items by warehouse
     */
    List<ItemDTO> getItemsByWarehouseId(Long warehouseId);

    /**
     * Get items by department
     */
    List<ItemDTO> getItemsByDepartmentId(Long departmentId);

    /**
     * Search items by name
     */
    List<ItemDTO> searchItemsByName(String keyword);

    /**
     * Check if an item exists by name in an enterprise
     */
    boolean itemExistsByName(String name, Long enterpriseId);

    /**
     * Get items currently in use by a specific user
     * @param userId The ID of the user
     * @return List of items in use by the user
     */
    List<ItemDTO> getItemsInUseByUserId(Long userId);

    /**
     * Return items from department back to warehouse
     * @param itemId The ID of the item to return
     * @param quantity The quantity to return
     * @param departmentId The ID of the department returning the item
     * @param warehouseId The ID of the warehouse to return to
     */
    @Transactional
    void returnItemToWarehouse(Long itemId, Integer quantity, Long departmentId, Long warehouseId);

    /**
     * Return items from employee back to department
     * @param itemId The ID of the item to return
     * @param quantity The quantity to return
     * @param userId The ID of the user returning the item
     * @param departmentId The ID of the department to return to
     */
    @Transactional
    void returnItemToDepartment(Long itemId, Integer quantity, Long userId, Long departmentId);
} 