package com.enterprise.inventorymanagement.controller;

import com.enterprise.inventorymanagement.exceptions.ResourceNotFoundException;
import com.enterprise.inventorymanagement.model.dto.ItemDTO;
import com.enterprise.inventorymanagement.service.InventoryManagementService;
import com.enterprise.inventorymanagement.service.UserDetailsImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inventory")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true", maxAge = 3600)
public class InventoryController {

    private final InventoryManagementService inventoryService;

    @Autowired
    public InventoryController(InventoryManagementService inventoryService) {
        this.inventoryService = inventoryService;
    }

    /**
     * Create a new inventory item
     */
    @PostMapping("/items")
    @PreAuthorize("hasAuthority('MANAGE_INVENTORY')")
    public ResponseEntity<Map<String, Object>> createItem(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody ItemDTO itemDTO) {
        try {
            ItemDTO createdItem = inventoryService.createItem(userDetails.getEnterpriseId(), itemDTO);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Item created successfully");
            response.put("item", createdItem);
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", "An unexpected error occurred");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Update an existing inventory item
     */
    @PutMapping("/items/{id}")
    @PreAuthorize("hasAuthority('MANAGE_INVENTORY')")
    public ResponseEntity<Map<String, Object>> updateItem(
            @PathVariable Long id,
            @Valid @RequestBody ItemDTO itemDTO) {
        try {
            ItemDTO updatedItem = inventoryService.updateItem(id, itemDTO);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Item updated successfully");
            response.put("item", updatedItem);
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", "An unexpected error occurred");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Delete an inventory item
     */
    @DeleteMapping("/items/{id}")
    @PreAuthorize("hasAuthority('MANAGE_INVENTORY')")
    public ResponseEntity<Map<String, Object>> deleteItem(@PathVariable Long id) {
        try {
            inventoryService.deleteItem(id);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Item deleted successfully");
            response.put("itemId", id);
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", "An unexpected error occurred");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get all items in the enterprise
     */
    @GetMapping("/items")
    @PreAuthorize("hasAuthority('VIEW_INVENTORY')")
    public ResponseEntity<Map<String, Object>> getAllItems(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam(required = false) String search) {
        try {
            List<ItemDTO> items;
            if (search != null && !search.trim().isEmpty()) {
                items = inventoryService.searchItemsByName(search);
            } else {
                items = inventoryService.getAllItemsByEnterpriseId(userDetails.getEnterpriseId());
            }
            Map<String, Object> response = new HashMap<>();
            response.put("items", items);
            response.put("count", items.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", "An unexpected error occurred");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get items in a specific warehouse
     */
    @GetMapping("/warehouse/{warehouseId}/items")
    @PreAuthorize("hasAuthority('VIEW_INVENTORY')")
    public ResponseEntity<Map<String, Object>> getWarehouseItems(@PathVariable Long warehouseId) {
        try {
            List<ItemDTO> items = inventoryService.getItemsByWarehouseId(warehouseId);
            Map<String, Object> response = new HashMap<>();
            response.put("items", items);
            response.put("count", items.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", "An unexpected error occurred");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get items in a specific department
     */
    @GetMapping("/department/{departmentId}/items")
    @PreAuthorize("hasAuthority('VIEW_INVENTORY')")
    public ResponseEntity<Map<String, Object>> getDepartmentItems(@PathVariable Long departmentId) {
        try {
            List<ItemDTO> items = inventoryService.getItemsByDepartmentId(departmentId);
            Map<String, Object> response = new HashMap<>();
            response.put("items", items);
            response.put("count", items.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", "An unexpected error occurred");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get a specific item by ID
     */
    @GetMapping("/items/{id}")
    @PreAuthorize("hasAuthority('VIEW_INVENTORY')")
    public ResponseEntity<Map<String, Object>> getItem(@PathVariable Long id) {
        try {
            return inventoryService.getItemById(id)
                    .map(item -> {
                        Map<String, Object> response = new HashMap<>();
                        response.put("item", item);
                        return ResponseEntity.ok(response);
                    })
                    .orElseGet(() -> {
                        Map<String, Object> response = new HashMap<>();
                        response.put("error", "Item not found");
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
                    });
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", "An unexpected error occurred");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get items in use by the current user
     */
    @GetMapping("/items/in-use")
    @PreAuthorize("hasAuthority('VIEW_INVENTORY')")
    public ResponseEntity<Map<String, Object>> getItemsInUse(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        try {
            List<ItemDTO> items = inventoryService.getItemsInUseByUserId(userDetails.getId());
            Map<String, Object> response = new HashMap<>();
            response.put("items", items);
            response.put("count", items.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", "An unexpected error occurred");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}

