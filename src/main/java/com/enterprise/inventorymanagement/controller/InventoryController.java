package com.enterprise.inventorymanagement.controller;

import com.enterprise.inventorymanagement.model.InventoryItem;
import com.enterprise.inventorymanagement.service.InventoryItemService;
import com.enterprise.inventorymanagement.exceptions.ResourceNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/inventory")
public class InventoryController {

    private final InventoryItemService itemService;

    @Autowired
    public InventoryController(InventoryItemService itemService) {
        this.itemService = itemService;
    }

    @GetMapping("/items")
    public List<InventoryItem> getAllItems() {
        return itemService.getAllItems();
    }

    @GetMapping("/items/{id}")
    public InventoryItem getItemById(@PathVariable Long id) {
        return itemService.getItemById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found with id " + id));
    }

    @PostMapping("/items")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public InventoryItem createItem(@Valid @RequestBody InventoryItem item) {
        return itemService.saveItem(item);
    }

    @PutMapping("/items/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public InventoryItem updateItem(@PathVariable Long id, @Valid @RequestBody InventoryItem item) {
        return itemService.updateItem(id, item);
    }

    @DeleteMapping("/items/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteItem(@PathVariable Long id) {
        itemService.deleteItem(id);
    }
}

