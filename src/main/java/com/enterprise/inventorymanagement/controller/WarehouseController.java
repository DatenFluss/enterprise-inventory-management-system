package com.enterprise.inventorymanagement.controller;

import com.enterprise.inventorymanagement.model.Warehouse;
import com.enterprise.inventorymanagement.model.dto.ItemDTO;
import com.enterprise.inventorymanagement.model.dto.ItemRequestDTO;
import com.enterprise.inventorymanagement.model.dto.WarehouseDTO;
import com.enterprise.inventorymanagement.model.request.RequestStatus;
import com.enterprise.inventorymanagement.service.WarehouseService;
import com.enterprise.inventorymanagement.service.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/warehouses")
public class WarehouseController {

    private final WarehouseService warehouseService;

    @Autowired
    public WarehouseController(WarehouseService warehouseService) {
        this.warehouseService = warehouseService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('MANAGE_WAREHOUSES')")
    public ResponseEntity<WarehouseDTO> createWarehouse(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody Warehouse warehouse) {
        return ResponseEntity.ok(warehouseService.createWarehouse(userDetails.getEnterpriseId(), warehouse));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('VIEW_WAREHOUSES')")
    public ResponseEntity<List<WarehouseDTO>> getEnterpriseWarehouses(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(warehouseService.getWarehousesByEnterpriseId(userDetails.getEnterpriseId()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('VIEW_WAREHOUSES')")
    public ResponseEntity<WarehouseDTO> getWarehouse(@PathVariable Long id) {
        return ResponseEntity.ok(warehouseService.getWarehouseById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGE_WAREHOUSES')")
    public ResponseEntity<WarehouseDTO> updateWarehouse(
            @PathVariable Long id,
            @Valid @RequestBody Warehouse warehouse) {
        return ResponseEntity.ok(warehouseService.updateWarehouse(id, warehouse));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGE_WAREHOUSES')")
    public ResponseEntity<Void> deleteWarehouse(@PathVariable Long id) {
        warehouseService.deleteWarehouse(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/manager/{userId}")
    @PreAuthorize("hasAuthority('MANAGE_WAREHOUSES')")
    public ResponseEntity<WarehouseDTO> assignManager(
            @PathVariable Long id,
            @PathVariable Long userId) {
        return ResponseEntity.ok(warehouseService.assignManager(id, userId));
    }

    @PostMapping("/{id}/operator")
    @PreAuthorize("hasAuthority('MANAGE_WAREHOUSES')")
    public ResponseEntity<WarehouseDTO> assignOperator(
            @PathVariable Long id,
            @RequestBody Map<String, Long> request) {
        Long userId = request.get("userId");
        return ResponseEntity.ok(warehouseService.assignOperator(id, userId));
    }

    @GetMapping("/operator")
    @PreAuthorize("hasAuthority('MANAGE_WAREHOUSE_ITEMS')")
    public ResponseEntity<WarehouseDTO> getOperatorWarehouse(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(warehouseService.getWarehouseByOperatorId(userDetails.getId()));
    }

    @GetMapping("/{id}/items")
    @PreAuthorize("hasAuthority('VIEW_WAREHOUSE_ITEMS')")
    public ResponseEntity<List<ItemDTO>> getWarehouseItems(@PathVariable Long id) {
        return ResponseEntity.ok(warehouseService.getWarehouseItems(id));
    }

    @PostMapping("/{id}/items")
    @PreAuthorize("hasAuthority('MANAGE_WAREHOUSE_ITEMS')")
    public ResponseEntity<ItemDTO> addWarehouseItem(
            @PathVariable Long id,
            @Valid @RequestBody ItemDTO itemDTO) {
        return ResponseEntity.ok(warehouseService.addWarehouseItem(id, itemDTO));
    }

    @GetMapping("/{id}/requests")
    @PreAuthorize("hasAuthority('MANAGE_WAREHOUSE_ITEMS')")
    public ResponseEntity<List<ItemRequestDTO>> getWarehouseRequests(
            @PathVariable Long id,
            @RequestParam(required = false) RequestStatus status) {
        if (status != null) {
            return ResponseEntity.ok(warehouseService.getWarehouseRequestsByStatus(id, status));
        }
        return ResponseEntity.ok(warehouseService.getWarehouseRequests(id));
    }
}