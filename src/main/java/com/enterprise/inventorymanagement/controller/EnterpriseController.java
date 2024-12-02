package com.enterprise.inventorymanagement.controller;

import com.enterprise.inventorymanagement.exceptions.ResourceNotFoundException;
import com.enterprise.inventorymanagement.model.Enterprise;
import com.enterprise.inventorymanagement.model.EnterpriseInvite;
import com.enterprise.inventorymanagement.model.InviteStatus;
import com.enterprise.inventorymanagement.model.Role;
import com.enterprise.inventorymanagement.model.RoleName;
import com.enterprise.inventorymanagement.model.User;
import com.enterprise.inventorymanagement.model.dto.DepartmentDTO;
import com.enterprise.inventorymanagement.model.dto.EnterpriseDTO;
import com.enterprise.inventorymanagement.model.dto.EnterpriseInviteDTO;
import com.enterprise.inventorymanagement.model.dto.UserDTO;
import com.enterprise.inventorymanagement.model.request.DepartmentRequest;
import com.enterprise.inventorymanagement.model.request.EnterpriseInviteRequest;
import com.enterprise.inventorymanagement.model.request.EnterpriseRegistrationRequest;
import com.enterprise.inventorymanagement.repository.EnterpriseRepository;
import com.enterprise.inventorymanagement.repository.InviteRepository;
import com.enterprise.inventorymanagement.repository.RoleRepository;
import com.enterprise.inventorymanagement.repository.UserRepository;
import com.enterprise.inventorymanagement.security.JwtTokenProvider;
import com.enterprise.inventorymanagement.service.EnterpriseService;
import com.enterprise.inventorymanagement.service.UserDetailsImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.*;

@RestController
@RequestMapping("/api/enterprises")
public class EnterpriseController {
    private static final Logger log = LoggerFactory.getLogger(EnterpriseController.class);

    private final EnterpriseService enterpriseService;
    private final EnterpriseRepository enterpriseRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final InviteRepository inviteRepository;
    private final JwtTokenProvider tokenProvider;

    @Autowired
    public EnterpriseController(
            EnterpriseService enterpriseService,
            EnterpriseRepository enterpriseRepository,
            UserRepository userRepository,
            RoleRepository roleRepository,
            InviteRepository inviteRepository,
            JwtTokenProvider tokenProvider) {
        this.enterpriseService = enterpriseService;
        this.enterpriseRepository = enterpriseRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.inviteRepository = inviteRepository;
        this.tokenProvider = tokenProvider;
    }

    /**
     * Register a new enterprise.
     */
    @PostMapping("/register")
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
            List<EnterpriseInviteDTO> invites = enterpriseService.getInvitesForUser(userDetails.getEmail());
            return ResponseEntity.ok(invites);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    /**
     * Create an invite for a user to join an enterprise
     */
    @PostMapping("/invites")
    @PreAuthorize("hasAuthority('MANAGE_ENTERPRISE')")
    public ResponseEntity<?> createInvite(
            @Valid @RequestBody EnterpriseInviteRequest request,
            @AuthenticationPrincipal UserDetailsImpl inviter) {
        try {
            Long enterpriseId = inviter.getEnterpriseId();
            if (enterpriseId == null) {
                return ResponseEntity.status(400).body("User is not associated with any enterprise");
            }

            enterpriseService.createInviteByEmail(enterpriseId, request.getEmail(), request.getRole(), inviter.getId());
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
            log.debug("Processing invite {} with action {} for user {}",
                    inviteId, action, userDetails.getEmail());

            if (!action.equalsIgnoreCase("accept") && !action.equalsIgnoreCase("decline")) {
                log.warn("Invalid action attempted: {}", action);
                return ResponseEntity.badRequest()
                        .body("Invalid action. Use 'accept' or 'decline'");
            }

            boolean accepted = action.equalsIgnoreCase("accept");

            EnterpriseInvite invite = inviteRepository.findById(inviteId)
                    .orElseThrow(() -> {
                        log.error("Invite not found with ID: {}", inviteId);
                        return new ResourceNotFoundException("Invite not found");
                    });

            log.debug("Found invite: {}", invite);

            if (!invite.getEmail().equals(userDetails.getEmail())) {
                log.warn("Unauthorized invite access attempt by user: {}", userDetails.getEmail());
                return ResponseEntity.status(403)
                        .body("Not authorized to handle this invite");
            }

            if (invite.getStatus() != InviteStatus.PENDING) {
                log.warn("Attempt to process non-pending invite. Status: {}", invite.getStatus());
                return ResponseEntity.badRequest()
                        .body("Invite is no longer pending");
            }

            if (accepted && userDetails.getEnterpriseId() != null) {
                log.warn("User already in enterprise attempting to accept invite. User: {}", userDetails.getEmail());
                return ResponseEntity.badRequest()
                        .body("User is already part of an enterprise");
            }

            invite.setStatus(accepted ? InviteStatus.ACCEPTED : InviteStatus.DECLINED);

            if (accepted) {
                log.debug("Processing accepted invite for user: {}", userDetails.getEmail());

                User user = userRepository.findById(userDetails.getId())
                        .orElseThrow(() -> new ResourceNotFoundException("User not found"));

                Enterprise enterprise = enterpriseRepository.findById(invite.getEnterpriseId())
                        .orElseThrow(() -> new ResourceNotFoundException("Enterprise not found"));

                Role role = roleRepository.findByName(invite.getRole())
                        .orElseThrow(() -> new ResourceNotFoundException("Role not found"));

                // Set the required fields
                user.setEnterprise(enterprise);
                user.setRole(role);
                userRepository.save(user);
                inviteRepository.save(invite);

                // Generate new JWT token with updated roles
                Authentication authentication = new UsernamePasswordAuthenticationToken(
                    UserDetailsImpl.build(user),
                    null,
                    UserDetailsImpl.build(user).getAuthorities()
                );
                String newToken = tokenProvider.generateToken(authentication);

                Map<String, Object> response = new HashMap<>();
                response.put("message", "Invite accepted successfully");
                response.put("token", newToken);
                response.put("role", role.getName());
                return ResponseEntity.ok(response);
            } else {
                inviteRepository.save(invite);
                return ResponseEntity.ok("Invite declined successfully");
            }
        } catch (Exception e) {
            log.error("Error processing invite: ", e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Get current user's enterprise
     */
    @GetMapping("/current")
    public ResponseEntity<?> getCurrentEnterprise(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        try {
            Long enterpriseId = userDetails.getEnterpriseId();
            if (enterpriseId == null) {
                return ResponseEntity.notFound().build();
            }

            // Use the service method that already handles lazy loading properly
            EnterpriseDTO enterprise = enterpriseService.getEnterpriseById(enterpriseId);
            
            log.debug("Retrieved enterprise for user {}: {}", 
                userDetails.getUsername(), 
                enterprise != null ? enterprise.getName() : "null");
            
            return ResponseEntity.ok(enterprise);
        } catch (ResourceNotFoundException e) {
            log.error("Error retrieving enterprise for user {}: {}", 
                userDetails.getUsername(), e.getMessage());
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error retrieving enterprise for user {}: {}", 
                userDetails.getUsername(), e.getMessage(), e);
            return ResponseEntity.status(500).body("An unexpected error occurred");
        }
    }

    /**
     * Get employees of the current enterprise
     */
    @GetMapping("/employees")
    @PreAuthorize("hasAuthority('VIEW_ENTERPRISE')")
    public ResponseEntity<?> getEnterpriseEmployees(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        try {
            if (userDetails.getEnterpriseId() == null) {
                return ResponseEntity.badRequest().body("User is not associated with any enterprise");
            }
            List<UserDTO> employees = enterpriseService.getEnterpriseEmployees(userDetails.getEnterpriseId());
            return ResponseEntity.ok(employees);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    @GetMapping("/departments")
    @PreAuthorize("hasAnyAuthority('VIEW_DEPARTMENTS', 'MANAGE_DEPARTMENTS')")
    public ResponseEntity<?> getEnterpriseDepartments(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        try {
            Long enterpriseId = userDetails.getEnterpriseId();
            if (enterpriseId == null) {
                return ResponseEntity.notFound().build();
            }
            List<DepartmentDTO> departments = enterpriseService.getEnterpriseDepartments(enterpriseId);
            return ResponseEntity.ok(departments);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    /**
     * Create a new department in an enterprise
     */
    @PostMapping("/departments")
    @PreAuthorize("hasAuthority('MANAGE_DEPARTMENTS')")
    public ResponseEntity<?> createDepartment(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody DepartmentRequest request) {
        try {
            Long enterpriseId = userDetails.getEnterpriseId();
            if (enterpriseId == null) {
                return ResponseEntity.status(400).body("User is not associated with any enterprise");
            }
            DepartmentDTO department = enterpriseService.createDepartment(enterpriseId, request);
            return ResponseEntity.ok(department);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/departments/{departmentId}/manager")
    @PreAuthorize("hasAuthority('MANAGE_DEPARTMENTS')")
    public ResponseEntity<?> assignDepartmentManager(
            @PathVariable Long departmentId,
            @RequestBody Map<String, Long> request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        try {
            Long userId = request.get("userId");
            if (userId == null) {
                return ResponseEntity.badRequest().body("User ID is required");
            }

            if (userDetails.getEnterpriseId() == null) {
                return ResponseEntity.badRequest().body("User is not associated with any enterprise");
            }

            DepartmentDTO department = enterpriseService.assignDepartmentManager(departmentId, userId, userDetails.getEnterpriseId());
            return ResponseEntity.ok(department);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}
