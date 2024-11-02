package com.enterprise.inventorymanagement.service;

import com.enterprise.inventorymanagement.exceptions.ResourceNotFoundException;
import com.enterprise.inventorymanagement.model.Enterprise;
import com.enterprise.inventorymanagement.model.EnterpriseInvite;
import com.enterprise.inventorymanagement.model.RoleName;
import com.enterprise.inventorymanagement.model.User;
import com.enterprise.inventorymanagement.model.dto.EnterpriseDTO;
import com.enterprise.inventorymanagement.model.dto.EnterpriseInviteDTO;
import com.enterprise.inventorymanagement.model.dto.UserDTO;
import com.enterprise.inventorymanagement.repository.*;
import com.enterprise.inventorymanagement.model.request.EnterpriseRegistrationRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of EnterpriseService interface.
 */
@Service
public class EnterpriseServiceImpl extends ServiceCommon implements EnterpriseService {

    private final InviteRepository inviteRepository;

    @Autowired
    public EnterpriseServiceImpl(
            UserRepository userRepository,
            InventoryItemRepository itemRepository,
            RoleRepository roleRepository,
            EnterpriseRepository enterpriseRepository,
            InviteRepository inviteRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationFacade authenticationFacade
    ) {
        super(
                userRepository,
                itemRepository,
                roleRepository,
                enterpriseRepository,
                passwordEncoder,
                authenticationFacade
        );
        this.inviteRepository = inviteRepository;
    }

    @Override
    @Transactional
    @PreAuthorize("hasAuthority('MANAGE_ENTERPRISE')")
    public void registerEnterprise(EnterpriseRegistrationRequest request) {
        // Input Validation
        if (request.getEnterpriseName() == null || request.getEnterpriseName().trim().isEmpty()) {
            throw new IllegalArgumentException("Enterprise name is required.");
        }
        if (request.getAddress() == null || request.getAddress().trim().isEmpty()) {
            throw new IllegalArgumentException("Enterprise address is required.");
        }
        if (request.getContactEmail() == null || request.getContactEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Enterprise contact email is required.");
        }
        if (request.getOwnerUsername() == null || request.getOwnerUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("Owner username is required.");
        }
        if (request.getOwnerPassword() == null || request.getOwnerPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("Owner password is required.");
        }
        if (request.getOwnerEmail() == null || request.getOwnerEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Owner email is required.");
        }

        Enterprise enterprise = new Enterprise();
        enterprise.setName(request.getEnterpriseName());
        enterprise.setAddress(request.getAddress());
        enterprise.setContactEmail(request.getContactEmail());

        Enterprise savedEnterprise = enterpriseRepository.save(enterprise);

        // Create Enterprise Owner
        User owner = new User();
        owner.setUsername(request.getOwnerUsername());
        owner.setPassword(passwordEncoder.encode(request.getOwnerPassword()));
        owner.setEmail(request.getOwnerEmail());
        owner.setActive(true);
        owner.setEnterprise(savedEnterprise);
        owner.setRole(roleRepository.findByName(RoleName.OWNER.label).orElseThrow());

        userRepository.save(owner);
    }

    @Override
    @Transactional
    public EnterpriseDTO getEnterpriseById(Long enterpriseId) throws ResourceNotFoundException {
        Enterprise enterprise = enterpriseRepository.findByIdWithEmployees(enterpriseId)
                .orElseThrow(() -> new ResourceNotFoundException("Enterprise not found with ID: " + enterpriseId));

        return convertToDTO(enterprise);
    }

    @Override
    public List<EnterpriseDTO> getAllEnterprises() {
        List<Enterprise> enterprises = enterpriseRepository.findAllWithEmployees();
        return enterprises.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }


    @Override
    @Transactional
    public EnterpriseDTO updateEnterprise(Long enterpriseId, EnterpriseDTO updatedEnterprise) throws ResourceNotFoundException {
        Enterprise enterprise = enterpriseRepository.findById(enterpriseId)
                .orElseThrow(() -> new ResourceNotFoundException("Enterprise not found with ID: " + enterpriseId));

        enterprise.setName(updatedEnterprise.getName());
        enterprise.setAddress(updatedEnterprise.getAddress());
        enterprise.setContactEmail(updatedEnterprise.getContactEmail());

        Enterprise savedEnterprise = enterpriseRepository.save(enterprise);

        return convertToDTO(savedEnterprise);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAuthority('MANAGE_ENTERPRISE')")
    public void deleteEnterprise(Long enterpriseId) throws ResourceNotFoundException {
        if (!enterpriseRepository.existsById(enterpriseId)) {
            throw new ResourceNotFoundException("Enterprise not found with ID: " + enterpriseId);
        }
        enterpriseRepository.deleteById(enterpriseId);
    }

    @Override
    @Transactional
    public void addEmployeeToEnterprise(Long enterpriseId, Long employeeId) throws ResourceNotFoundException {
        Enterprise enterprise = enterpriseRepository.findById(enterpriseId)
                .orElseThrow(() -> new ResourceNotFoundException("Enterprise not found with ID: " + enterpriseId));

        User employee = userRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + employeeId));

        employee.setEnterprise(enterprise);
        userRepository.save(employee);
    }

    @Override
    @Transactional
    public void removeEmployeeFromEnterprise(Long enterpriseId, Long employeeId) throws ResourceNotFoundException {
        User employee = userRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + employeeId));

        if (employee.getEnterprise() == null || !employee.getEnterprise().getId().equals(enterpriseId)) {
            throw new ResourceNotFoundException("Employee does not belong to the specified enterprise.");
        }

        employee.setEnterprise(null);
        userRepository.save(employee);
    }

    @Override
    @Transactional
    public List<EnterpriseInviteDTO> getInvitesForUser(Long userId) {
        List<EnterpriseInvite> invites = inviteRepository.findByUserId(userId);
        return invites.stream()
                .map(this::convertToInviteDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void createInvite(Long enterpriseId, Long userId, RoleName role, Long inviterId) {
        Enterprise enterprise = enterpriseRepository.findById(enterpriseId)
                .orElseThrow(() -> new ResourceNotFoundException("Enterprise not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Validate user isn't already in an enterprise
        if (user.getEnterprise() != null) {
            throw new IllegalStateException("User is already part of an enterprise");
        }

        // Check if invite already exists
        if (inviteRepository.existsByUserIdAndEnterpriseId(userId, enterpriseId)) {
            throw new IllegalStateException("Invite already exists");
        }

        EnterpriseInvite invite = new EnterpriseInvite();
        invite.setUserId(userId);
        invite.setEnterpriseId(enterpriseId);
        invite.setInviterId(inviterId);
        invite.setRole(role);

        inviteRepository.save(invite);
    }

    @Override
    @Transactional
    public void handleInviteResponse(Long inviteId, Long userId, boolean accepted) {
        EnterpriseInvite invite = inviteRepository.findById(inviteId)
                .orElseThrow(() -> new ResourceNotFoundException("Invite not found"));

        if (!invite.getUserId().equals(userId)) {
            throw new AccessDeniedException("Not authorized to handle this invite");
        }

        if (accepted) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            Enterprise enterprise = enterpriseRepository.findById(invite.getEnterpriseId())
                    .orElseThrow(() -> new ResourceNotFoundException("Enterprise not found"));

            user.setEnterprise(enterprise);
            user.setRole(roleRepository.findByName(invite.getRole().label)
                    .orElseThrow(() -> new ResourceNotFoundException("Role not found")));

            userRepository.save(user);
        }

        inviteRepository.delete(invite);
    }

    @Override
    public List<UserDTO> getEnterpriseEmployees(Long enterpriseId) {
        Enterprise enterprise = enterpriseRepository.findById(enterpriseId)
                .orElseThrow(() -> new ResourceNotFoundException("Enterprise not found"));

        return userRepository.findByEnterpriseId(enterpriseId)
                .stream()
                .map(this::convertToUserDTO)
                .collect(Collectors.toList());
    }

    private EnterpriseInviteDTO convertToInviteDTO(EnterpriseInvite invite) {
        Enterprise enterprise = enterpriseRepository.findById(invite.getEnterpriseId())
                .orElseThrow(() -> new ResourceNotFoundException("Enterprise not found"));

        User inviter = userRepository.findById(invite.getInviterId())
                .orElseThrow(() -> new ResourceNotFoundException("Inviter not found"));

        return EnterpriseInviteDTO.builder()
                .id(invite.getId())
                .enterpriseName(enterprise.getName())
                .role(invite.getRole().toString())
                .inviterName(inviter.getUsername())
                .createdAt(invite.getCreatedAt())
                .build();
    }

    private UserDTO convertToUserDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .active(user.getActive())
                .enterpriseId(user.getEnterprise() != null ? user.getEnterprise().getId() : null)
                .managerId(user.getManager() != null ? user.getManager().getId() : null)
                .roleName(user.getRole().getName())
                .build();
    }

    // Helper method to convert Enterprise to EnterpriseDTO
    private EnterpriseDTO convertToDTO(Enterprise enterprise) {
        EnterpriseDTO dto = new EnterpriseDTO();
        dto.setId(enterprise.getId());
        dto.setName(enterprise.getName());
        dto.setAddress(enterprise.getAddress());
        dto.setContactEmail(enterprise.getContactEmail());

        Set<Long> employeeIds = Collections.emptySet();
        if (enterprise.getEmployees() != null) {
            employeeIds = enterprise.getEmployees().stream()
                    .map(User::getId)
                    .collect(Collectors.toSet());
        }
        dto.setEmployeeIds(employeeIds);

        return dto;
    }
}