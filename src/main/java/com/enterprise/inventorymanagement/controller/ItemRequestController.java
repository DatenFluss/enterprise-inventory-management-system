package com.enterprise.inventorymanagement.controller;

import com.enterprise.inventorymanagement.exceptions.ResourceNotFoundException;
import com.enterprise.inventorymanagement.model.dto.ItemRequestDTO;
import com.enterprise.inventorymanagement.model.request.RequestStatus;
import com.enterprise.inventorymanagement.service.ItemRequestService;
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
@RequestMapping("/api/requests")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true", maxAge = 3600)
public class ItemRequestController {

    private final ItemRequestService itemRequestService;

    @Autowired
    public ItemRequestController(ItemRequestService itemRequestService) {
        this.itemRequestService = itemRequestService;
    }

    /**
     * Create a new item request (Department Manager)
     */
    @PostMapping
    @PreAuthorize("hasAuthority('VIEW_OWN_REQUESTS')")
    public ResponseEntity<Map<String, Object>> createRequest(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody ItemRequestDTO requestDTO) {
        try {
            ItemRequestDTO createdRequest = itemRequestService.createItemRequest(userDetails.getId(), requestDTO);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Request created successfully");
            response.put("request", createdRequest);
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
     * Get requests for a warehouse (Warehouse Operator)
     */
    @GetMapping("/warehouse/{warehouseId}")
    @PreAuthorize("hasAuthority('MANAGE_WAREHOUSE')")
    public ResponseEntity<Map<String, Object>> getWarehouseRequests(
            @PathVariable Long warehouseId,
            @RequestParam(required = false) RequestStatus status) {
        try {
            List<ItemRequestDTO> requests;
            if (status != null) {
                requests = itemRequestService.getRequestsByWarehouseIdAndStatus(warehouseId, status);
            } else {
                requests = itemRequestService.getRequestsByWarehouseId(warehouseId);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("requests", requests);
            response.put("count", requests.size());
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
     * Get requests for a department (Department Manager)
     */
    @GetMapping("/department/{departmentId}")
    @PreAuthorize("hasAuthority('MANAGE_DEPARTMENT')")
    public ResponseEntity<Map<String, Object>> getDepartmentRequests(
            @PathVariable Long departmentId,
            @RequestParam(required = false) RequestStatus status) {
        try {
            List<ItemRequestDTO> requests;
            if (status != null) {
                requests = itemRequestService.getRequestsByDepartmentIdAndStatus(departmentId, status);
            } else {
                requests = itemRequestService.getRequestsByDepartmentId(departmentId);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("requests", requests);
            response.put("count", requests.size());
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
     * Get requests for the current user (Employee)
     */
    @GetMapping
    @PreAuthorize("hasAnyAuthority('VIEW_REQUESTS', 'VIEW_PENDING_REQUESTS', 'VIEW_OWN_REQUESTS')")
    public ResponseEntity<Map<String, Object>> getUserRequests(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam(required = false) RequestStatus status) {
        try {
            List<ItemRequestDTO> requests;
            if (status != null) {
                requests = itemRequestService.getRequestsByUserIdAndStatus(userDetails.getId(), status);
            } else {
                requests = itemRequestService.getRequestsByUserId(userDetails.getId());
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("requests", requests);
            response.put("count", requests.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", "An unexpected error occurred");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Handle a request (Warehouse Operator)
     */
    @PostMapping("/{requestId}/process")
    @PreAuthorize("hasAuthority('MANAGE_WAREHOUSE')")
    public ResponseEntity<Map<String, Object>> handleRequest(
            @PathVariable Long requestId,
            @RequestParam boolean approved,
            @RequestParam(required = false) String responseComments) {
        try {
            itemRequestService.handleItemRequest(requestId, approved, responseComments);
            String status = approved ? "approved" : "rejected";
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", String.format("Request %s successfully", status));
            response.put("requestId", requestId);
            response.put("status", status);
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (IllegalStateException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", "An unexpected error occurred");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PutMapping("/{requestId}/approve")
    @PreAuthorize("hasAuthority('MANAGE_REQUESTS')")
    public ResponseEntity<?> approveRequest(@PathVariable Long requestId,
                                            @RequestBody(required = false) String comments) {
        try {
            itemRequestService.approveItemRequest(requestId, comments);
            return ResponseEntity.ok().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PutMapping("/{requestId}/reject")
    @PreAuthorize("hasAuthority('MANAGE_REQUESTS')")
    public ResponseEntity<?> rejectRequest(@PathVariable Long requestId,
                                           @RequestBody(required = false) String comments) {
        try {
            itemRequestService.rejectItemRequest(requestId, comments);
            return ResponseEntity.ok().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    /**
     * Get requests for the current user
     */
    @GetMapping("/my")
    @PreAuthorize("hasAnyAuthority('VIEW_MY_REQUESTS', 'VIEW_OWN_REQUESTS')")
    public ResponseEntity<Map<String, Object>> getMyRequests(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam(required = false) RequestStatus status) {
        try {
            List<ItemRequestDTO> requests;
            if (status != null) {
                requests = itemRequestService.getRequestsByUserIdAndStatus(userDetails.getId(), status);
            } else {
                requests = itemRequestService.getRequestsByUserId(userDetails.getId());
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("requests", requests);
            response.put("count", requests.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", "An unexpected error occurred");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
