package com.enterprise.inventorymanagemet.service;

import com.enterprise.inventorymanagemet.model.InventoryItem;

import java.util.List;
import java.util.Optional;


public interface InventoryItemService {
    InventoryItem saveItem(InventoryItem item);
    Optional<InventoryItem> getItemById(Long id);
    List<InventoryItem> getAllItems();
    InventoryItem updateItem(Long id, InventoryItem item);
    void deleteItem(Long id);
    Optional<InventoryItem> getItemByName(String name);
    List<InventoryItem> searchItemsByName(String keyword);
    boolean itemExistsByName(String name);
    void requestItem(Long itemId, int quantity, String comments);
    List<InventoryItem> viewAvailableItems();
}


