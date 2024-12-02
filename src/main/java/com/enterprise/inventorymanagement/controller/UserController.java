package com.enterprise.inventorymanagement.controller;

import com.enterprise.inventorymanagement.exceptions.ResourceNotFoundException;
import com.enterprise.inventorymanagement.model.RoleName;
import com.enterprise.inventorymanagement.model.dto.EnterpriseInviteDTO;
import com.enterprise.inventorymanagement.model.dto.JwtResponse;
import com.enterprise.inventorymanagement.model.dto.UserDTO;
import com.enterprise.inventorymanagement.model.request.LoginRequest;
import com.enterprise.inventorymanagement.model.request.UserRegistrationRequest;
import com.enterprise.inventorymanagement.security.JwtTokenProvider;
import com.enterprise.inventorymanagement.service.UserDetailsImpl;
import com.enterprise.inventorymanagement.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@Slf4j
public class UserController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    @Autowired
    public UserController(UserService userService, AuthenticationManager authenticationManager,
                          JwtTokenProvider tokenProvider) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
    }

    /**
     * User Registration (Sign-up)
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserRegistrationRequest request) {
        try {
            userService.registerUser(request);
            return ResponseEntity.ok("User registration successful");
        } catch (IllegalArgumentException e) {
            log.error("Registration failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * User Authentication (Sign-in)
     */
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            String jwt = tokenProvider.generateToken(authentication);
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

            return ResponseEntity.ok(new JwtResponse(
                    jwt,
                    userDetails.getId(),
                    userDetails.getFullName(),
                    userDetails.getUsername(),
                    userDetails.getEmail(),
                    userDetails.getEnterpriseId(),
                    userDetails.getRole()
            ));
        } catch (Exception e) {
            log.error("Authentication failed for user {}: {}", loginRequest.getUsername(), e.getMessage());
            return ResponseEntity.status(401).body("Invalid username or password");
        }
    }

    /**
     * Get Current User Information
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        try {
            UserDTO userDTO = userService.getUserById(userDetails.getId());
            return ResponseEntity.ok(userDTO);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    /**
     * Get User's Manager
     */
    @GetMapping("/manager")
    public ResponseEntity<?> getUserManager(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        try {
            UserDTO manager = userService.getUserManager(userDetails.getId());
            return ResponseEntity.ok(manager);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    /**
     * Get User's Subordinates
     */
    @GetMapping("/subordinates")
    @PreAuthorize("hasAnyAuthority('ROLE_MANAGER', 'ROLE_ADMIN', 'ROLE_ENTERPRISE_OWNER')")
    public ResponseEntity<?> getUserSubordinates(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        List<UserDTO> subordinates = userService.getSubordinates(userDetails.getId());
        return ResponseEntity.ok(subordinates);
    }

    /**
     * Assign Role to User
     */
    @PutMapping("/{id}/roles")
    @PreAuthorize("hasAuthority('MANAGE_USERS')")
    public ResponseEntity<?> assignRoleToUser(
            @PathVariable Long id,
            @RequestParam RoleName roleName) {
        try {
            userService.assignRole(id, roleName);
            return ResponseEntity.ok("Role assigned successfully");
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    /**
     * Update User Information
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGE_USERS') or #id == principal.id")
    public ResponseEntity<?> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserDTO userDTO) {
        try {
            UserDTO updatedUser = userService.updateUser(id, userDTO);
            return ResponseEntity.ok(updatedUser);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    /**
     * Delete User
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGE_USERS')")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok("User deleted successfully");
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    /**
     * Get Enterprise Invites for User
     */
    //@GetMapping("/invites")
    //public ResponseEntity<?> getUserInvites(@AuthenticationPrincipal UserDetailsImpl userDetails) {
    //    List<EnterpriseInviteDTO> invites = userService.getUserInvites(userDetails.getId());
    //    return ResponseEntity.ok(invites);
    //}

    /**
     * Respond to Enterprise Invite
     */
    @PostMapping("/invites/{inviteId}/{action}")
    public ResponseEntity<?> respondToInvite(
            @PathVariable Long inviteId,
            @PathVariable String action,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        try {
            boolean accepted = "accept".equalsIgnoreCase(action);
            userService.handleInviteResponse(inviteId, userDetails.getId(), accepted);
            return ResponseEntity.ok(accepted ? "Invite accepted" : "Invite declined");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
