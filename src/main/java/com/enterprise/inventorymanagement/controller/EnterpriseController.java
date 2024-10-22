package com.enterprise.inventorymanagement.controller;

import com.enterprise.inventorymanagement.exceptions.ResourceNotFoundException;
import com.enterprise.inventorymanagement.model.dto.EnterpriseDTO;
import com.enterprise.inventorymanagement.model.request.EnterpriseRegistrationRequest;
import com.enterprise.inventorymanagement.service.EnterpriseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/enterprise")
public class EnterpriseController {

    private final EnterpriseService enterpriseService;

    @Autowired
    public EnterpriseController(EnterpriseService enterpriseService) {
        this.enterpriseService = enterpriseService;
    }

    /**
     * Register a new enterprise.
     */
    @PostMapping("/register")
    @PreAuthorize("hasAuthority('MANAGE_ENTERPRISE')")
    public ResponseEntity<?> registerEnterprise(@Valid @RequestBody EnterpriseRegistrationRequest request) {
        enterpriseService.registerEnterprise(request);
        return ResponseEntity.ok("Enterprise registration successful");
    }

    /**
     * Get an enterprise by its ID.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('VIEW_ENTERPRISE')")
    public ResponseEntity<?> getEnterpriseById(@PathVariable Long id) {
        try {
            EnterpriseDTO enterpriseDTO = enterpriseService.getEnterpriseById(id);
            return ResponseEntity.ok(enterpriseDTO);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    /**
     * Get all enterprises.
     */
    @GetMapping
    @PreAuthorize("hasAuthority('VIEW_ENTERPRISE')")
    public ResponseEntity<List<EnterpriseDTO>> getAllEnterprises() {
        List<EnterpriseDTO> enterprises = enterpriseService.getAllEnterprises();
        return ResponseEntity.ok(enterprises);
    }

    /**
     * Update an existing enterprise.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGE_ENTERPRISE')")
    public ResponseEntity<?> updateEnterprise(
            @PathVariable Long id,
            @Valid @RequestBody EnterpriseDTO enterpriseDTO) {
        try {
            EnterpriseDTO updatedEnterprise = enterpriseService.updateEnterprise(id, enterpriseDTO);
            return ResponseEntity.ok(updatedEnterprise);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    /**
     * Delete an enterprise by its ID.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGE_ENTERPRISE')")
    public ResponseEntity<?> deleteEnterprise(@PathVariable Long id) {
        try {
            enterpriseService.deleteEnterprise(id);
            return ResponseEntity.ok("Enterprise deleted successfully");
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    @PostMapping("/{enterpriseId}/employees/{employeeId}")
    @PreAuthorize("hasAuthority('MANAGE_ENTERPRISE')")
    public ResponseEntity<?> addEmployeeToEnterprise(
            @PathVariable Long enterpriseId,
            @PathVariable Long employeeId) {
        try {
            enterpriseService.addEmployeeToEnterprise(enterpriseId, employeeId);
            return ResponseEntity.ok("Employee added to enterprise successfully");
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    @DeleteMapping("/{enterpriseId}/employees/{employeeId}")
    @PreAuthorize("hasAuthority('MANAGE_ENTERPRISE')")
    public ResponseEntity<?> removeEmployeeFromEnterprise(
            @PathVariable Long enterpriseId,
            @PathVariable Long employeeId) {
        try {
            enterpriseService.removeEmployeeFromEnterprise(enterpriseId, employeeId);
            return ResponseEntity.ok("Employee removed from enterprise successfully");
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

}
