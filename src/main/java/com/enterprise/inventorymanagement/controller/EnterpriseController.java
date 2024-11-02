package com.enterprise.inventorymanagement.controller;

import com.enterprise.inventorymanagement.exceptions.ResourceNotFoundException;
import com.enterprise.inventorymanagement.model.EnterpriseInvite;
import com.enterprise.inventorymanagement.model.dto.EnterpriseDTO;
import com.enterprise.inventorymanagement.model.dto.EnterpriseInviteDTO;
import com.enterprise.inventorymanagement.model.dto.UserDTO;
import com.enterprise.inventorymanagement.model.request.EnterpriseRegistrationRequest;
import com.enterprise.inventorymanagement.service.EnterpriseService;
import com.enterprise.inventorymanagement.service.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/enterprises")
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

    /**
     * Get enterprise invites for a user
     */
    @GetMapping("/invites")
    public ResponseEntity<?> getEnterpriseInvites(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        try {
            List<EnterpriseInviteDTO> invites = enterpriseService.getInvitesForUser(userDetails.getId());
            return ResponseEntity.ok(invites);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    /**
     * Create an invite for a user to join an enterprise
     */
    @PostMapping("/{enterpriseId}/invites")
    @PreAuthorize("hasAuthority('MANAGE_ENTERPRISE')")
    public ResponseEntity<?> createInvite(
            @PathVariable Long enterpriseId,
            @Valid @RequestBody EnterpriseInvite request,
            @AuthenticationPrincipal UserDetailsImpl inviter) {
        try {
            enterpriseService.createInvite(enterpriseId, request.getUserId(), request.getRole(), inviter.getId());
            return ResponseEntity.ok("Invite sent successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Handle invite response (accept/decline)
     */
    @PostMapping("/invites/{inviteId}/{action}")
    public ResponseEntity<?> handleInvite(
            @PathVariable Long inviteId,
            @PathVariable String action,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        try {
            boolean accepted = "accept".equalsIgnoreCase(action);
            enterpriseService.handleInviteResponse(inviteId, userDetails.getId(), accepted);
            return ResponseEntity.ok(accepted ? "Invite accepted" : "Invite declined");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Get current user's enterprise
     */
    @GetMapping("/current")
    public ResponseEntity<?> getCurrentEnterprise(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        try {
            if (userDetails.getEnterpriseId() == null) {
                return ResponseEntity.notFound().build();
            }
            EnterpriseDTO enterprise = enterpriseService.getEnterpriseById(userDetails.getEnterpriseId());
            return ResponseEntity.ok(enterprise);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    /**
     * Get all employees in an enterprise
     */
    @GetMapping("/{enterpriseId}/employees")
    @PreAuthorize("hasAnyAuthority('VIEW_USERS', 'MANAGE_USERS')")
    public ResponseEntity<?> getEnterpriseEmployees(@PathVariable Long enterpriseId) {
        try {
            List<UserDTO> employees = enterpriseService.getEnterpriseEmployees(enterpriseId);
            return ResponseEntity.ok(employees);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

}
