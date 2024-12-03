package com.enterprise.inventorymanagement.controller;

import com.enterprise.inventorymanagement.model.dto.ItemDTO;
import com.enterprise.inventorymanagement.service.InventoryManagementService;
import com.enterprise.inventorymanagement.service.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/items")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true", maxAge = 3600)
public class ItemController {

    private final InventoryManagementService inventoryService;

    @Autowired
    public ItemController(InventoryManagementService inventoryService) {
        this.inventoryService = inventoryService;
    }

    /**
     * Get items in use by the current user
     */
    @GetMapping("/in-use")
    @PreAuthorize("hasAuthority('VIEW_INVENTORY')")
    public ResponseEntity<Map<String, Object>> getItemsInUse(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        List<ItemDTO> items = inventoryService.getItemsInUseByUserId(userDetails.getId());
        Map<String, Object> response = new HashMap<>();
        response.put("items", items);
        response.put("count", items.size());
        return ResponseEntity.ok(response);
    }
} 