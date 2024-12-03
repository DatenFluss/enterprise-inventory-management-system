package com.enterprise.inventorymanagement.controller;

import com.enterprise.inventorymanagement.model.dto.EmployeeItemRequestDTO;
import com.enterprise.inventorymanagement.model.request.RequestStatus;
import com.enterprise.inventorymanagement.service.EmployeeItemRequestService;
import com.enterprise.inventorymanagement.service.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employee-requests")
public class EmployeeItemRequestController {

    private final EmployeeItemRequestService requestService;

    @Autowired
    public EmployeeItemRequestController(EmployeeItemRequestService requestService) {
        this.requestService = requestService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('REQUEST_ITEMS')")
    public ResponseEntity<EmployeeItemRequestDTO> createRequest(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody EmployeeItemRequestDTO requestDTO) {
        EmployeeItemRequestDTO createdRequest = requestService.createRequest(userDetails.getId(), requestDTO);
        return ResponseEntity.ok(createdRequest);
    }

    @PostMapping("/{requestId}/process")
    @PreAuthorize("hasAuthority('MANAGE_EMPLOYEE_REQUESTS')")
    public ResponseEntity<Void> handleRequest(
            @PathVariable Long requestId,
            @RequestParam boolean approved,
            @RequestParam(required = false) String responseComments) {
        requestService.handleRequest(requestId, approved, responseComments);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/department/{departmentId}")
    @PreAuthorize("hasAuthority('MANAGE_EMPLOYEE_REQUESTS')")
    public ResponseEntity<List<EmployeeItemRequestDTO>> getDepartmentRequests(
            @PathVariable Long departmentId,
            @RequestParam(required = false) RequestStatus status) {
        List<EmployeeItemRequestDTO> requests = status != null
                ? requestService.getRequestsByDepartmentIdAndStatus(departmentId, status)
                : requestService.getRequestsByDepartmentId(departmentId);
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/my")
    @PreAuthorize("hasAuthority('VIEW_OWN_ITEM_REQUESTS')")
    public ResponseEntity<List<EmployeeItemRequestDTO>> getMyRequests(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam(required = false) RequestStatus status) {
        List<EmployeeItemRequestDTO> requests = status != null
                ? requestService.getRequestsByUserIdAndStatus(userDetails.getId(), status)
                : requestService.getRequestsByUserId(userDetails.getId());
        return ResponseEntity.ok(requests);
    }
} 