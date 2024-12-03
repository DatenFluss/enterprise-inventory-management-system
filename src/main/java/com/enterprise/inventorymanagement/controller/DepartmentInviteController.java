package com.enterprise.inventorymanagement.controller;

import com.enterprise.inventorymanagement.model.dto.DepartmentInviteDTO;
import com.enterprise.inventorymanagement.service.DepartmentInviteService;
import com.enterprise.inventorymanagement.service.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/department-invites")
@RequiredArgsConstructor
public class DepartmentInviteController {

    private final DepartmentInviteService departmentInviteService;

    @GetMapping("/my")
    public ResponseEntity<List<DepartmentInviteDTO>> getMyInvites(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        List<DepartmentInviteDTO> invites = departmentInviteService.getPendingInvitesForUser(userDetails.getId());
        return ResponseEntity.ok(invites);
    }

    @GetMapping("/department/{departmentId}/pending")
    @PreAuthorize("hasAuthority('MANAGE_DEPARTMENT')")
    public ResponseEntity<List<DepartmentInviteDTO>> getPendingInvitesForDepartment(
            @PathVariable Long departmentId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        List<DepartmentInviteDTO> invites = departmentInviteService.getPendingInvitesForDepartment(departmentId);
        return ResponseEntity.ok(invites);
    }

    @PostMapping("/department/{departmentId}/invite/{userId}")
    @PreAuthorize("hasAuthority('MANAGE_DEPARTMENT')")
    public ResponseEntity<DepartmentInviteDTO> createInvite(
            @PathVariable Long departmentId,
            @PathVariable Long userId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        DepartmentInviteDTO invite = departmentInviteService.createInvite(departmentId, userId);
        return ResponseEntity.ok(invite);
    }

    @PutMapping("/{inviteId}/accept")
    @PreAuthorize("hasAuthority('VIEW_DEPARTMENT_INVITES')")
    public ResponseEntity<DepartmentInviteDTO> acceptInvite(
            @PathVariable Long inviteId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        DepartmentInviteDTO invite = departmentInviteService.acceptInvite(inviteId, userDetails.getId());
        return ResponseEntity.ok(invite);
    }

    @PutMapping("/{inviteId}/reject")
    @PreAuthorize("hasAuthority('VIEW_DEPARTMENT_INVITES')")
    public ResponseEntity<DepartmentInviteDTO> rejectInvite(
            @PathVariable Long inviteId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        DepartmentInviteDTO invite = departmentInviteService.rejectInvite(inviteId, userDetails.getId());
        return ResponseEntity.ok(invite);
    }
} 