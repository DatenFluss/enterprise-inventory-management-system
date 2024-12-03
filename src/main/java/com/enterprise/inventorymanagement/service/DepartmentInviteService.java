package com.enterprise.inventorymanagement.service;

import com.enterprise.inventorymanagement.model.dto.DepartmentInviteDTO;
import java.util.List;

public interface DepartmentInviteService {
    List<DepartmentInviteDTO> getPendingInvitesForDepartment(Long departmentId);
    List<DepartmentInviteDTO> getPendingInvitesForUser(Long userId);
    DepartmentInviteDTO createInvite(Long departmentId, Long userId);
    DepartmentInviteDTO acceptInvite(Long inviteId, Long userId);
    DepartmentInviteDTO rejectInvite(Long inviteId, Long userId);
} 