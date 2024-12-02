package com.enterprise.inventorymanagement.service;

import com.enterprise.inventorymanagement.exceptions.ResourceNotFoundException;
import com.enterprise.inventorymanagement.model.*;
import com.enterprise.inventorymanagement.model.dto.DepartmentDTO;
import com.enterprise.inventorymanagement.model.dto.EnterpriseDTO;
import com.enterprise.inventorymanagement.model.dto.EnterpriseInviteDTO;
import com.enterprise.inventorymanagement.model.dto.UserDTO;
import com.enterprise.inventorymanagement.model.request.DepartmentRequest;
import com.enterprise.inventorymanagement.repository.*;
import com.enterprise.inventorymanagement.model.request.EnterpriseRegistrationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(EnterpriseServiceImpl.class);

    private final InviteRepository inviteRepository;
    private final DepartmentRepository departmentRepository;

    @Autowired
    public EnterpriseServiceImpl(
            UserRepository userRepository,
            InventoryItemRepository itemRepository,
            RoleRepository roleRepository,
            EnterpriseRepository enterpriseRepository,
            InviteRepository inviteRepository,
            DepartmentRepository departmentRepository,
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
        this.departmentRepository = departmentRepository;
    }

    @Override
    @Transactional
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
        if (request.getOwnerFullName() == null || request.getOwnerFullName().trim().isEmpty()) {
            throw new IllegalArgumentException("Owner full name is required.");
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
        owner.setFullName(request.getOwnerFullName());
        owner.setActive(true);
        owner.setEnterprise(savedEnterprise);
        try {
            Role ownerRole = roleRepository.findByName(RoleName.ROLE_ENTERPRISE_OWNER)
                    .orElseThrow(() -> new IllegalStateException(
                            "Required role 'ROLE_ENTERPRISE_OWNER' not found in the database. Please ensure all roles are properly initialized."));
            owner.setRole(ownerRole);
            userRepository.save(owner);
        } catch (Exception e) {
            // Rollback the enterprise creation if role assignment fails
            enterpriseRepository.delete(savedEnterprise);
            throw new IllegalStateException("Failed to assign owner role: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public EnterpriseDTO getEnterpriseById(Long enterpriseId) throws ResourceNotFoundException {
        log.debug("Getting enterprise by ID: {}", enterpriseId);
        
        // Get the enterprise with its employees
        Enterprise enterprise = enterpriseRepository.findByIdWithEmployees(enterpriseId)
                .orElseThrow(() -> new ResourceNotFoundException("Enterprise not found with ID: " + enterpriseId));
        
        // Get the actual employee count from the database
        List<User> employees = userRepository.findByEnterpriseId(enterpriseId);
        int employeeCount = employees.size();
        
        log.debug("Found enterprise: {}, Employee count from query: {}", 
            enterprise.getName(), employeeCount);

        // Get departments for this enterprise
        List<DepartmentDTO> departments = departmentRepository.findByEnterpriseId(enterpriseId)
                .stream()
                .map(this::convertToDepartmentDTO)
                .collect(Collectors.toList());

        // Get the current user's department if they are authenticated
        DepartmentDTO userDepartment = null;
        User currentUser = null;
        try {
            currentUser = getCurrentAuthenticatedUser();
        } catch (Exception e) {
            log.debug("No authenticated user found: {}", e.getMessage());
        }

        if (currentUser != null) {
            // First check if user is a manager of any department
            final User authenticatedUser = currentUser;  // Create final copy for lambda
            final Long userId = authenticatedUser.getId();
            userDepartment = departments.stream()
                    .filter(dept -> userId.equals(dept.getManagerId()))
                    .findFirst()
                    .orElse(null);

            // If not a manager, check if user is an employee of any department
            if (userDepartment == null) {
                userDepartment = departments.stream()
                        .filter(dept -> dept.getEmployeeIds() != null && dept.getEmployeeIds().contains(userId))
                        .findFirst()
                        .orElse(null);
            }

            log.debug("Found user department: {}", userDepartment != null ? userDepartment.getName() : "None");
        }

        // Create DTO with the actual employee count
        EnterpriseDTO dto = new EnterpriseDTO();
        dto.setId(enterprise.getId());
        dto.setName(enterprise.getName());
        dto.setAddress(enterprise.getAddress());
        dto.setContactEmail(enterprise.getContactEmail());
        dto.setTotalEmployees(employeeCount);
        dto.setDepartments(departments);
        dto.setUserDepartment(userDepartment);
        
        Set<Long> employeeIds = employees.stream()
                .map(User::getId)
                .collect(Collectors.toSet());
        dto.setEmployeeIds(employeeIds);

        log.debug("Returning DTO with total employees: {}", dto.getTotalEmployees());
        return dto;
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
        Enterprise enterprise = enterpriseRepository.findByIdWithEmployees(enterpriseId)
                .orElseThrow(() -> new ResourceNotFoundException("Enterprise not found with ID: " + enterpriseId));

        User employee = userRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + employeeId));

        // Update both sides of the relationship
        employee.setEnterprise(enterprise);
        enterprise.getEmployees().add(employee);

        // Save both entities
        userRepository.save(employee);
        enterpriseRepository.save(enterprise);
    }

    @Override
    @Transactional
    public void removeEmployeeFromEnterprise(Long enterpriseId, Long employeeId) throws ResourceNotFoundException {
        Enterprise enterprise = enterpriseRepository.findByIdWithEmployees(enterpriseId)
                .orElseThrow(() -> new ResourceNotFoundException("Enterprise not found with ID: " + enterpriseId));

        User employee = userRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + employeeId));

        if (employee.getEnterprise() == null || !employee.getEnterprise().getId().equals(enterpriseId)) {
            throw new ResourceNotFoundException("Employee does not belong to the specified enterprise.");
        }

        employee.setEnterprise(null);
        enterprise.getEmployees().remove(employee);
        userRepository.save(employee);
        enterpriseRepository.save(enterprise);
    }

    @Override
    @Transactional
    public List<EnterpriseInviteDTO> getInvitesForUser(String email) {
        List<EnterpriseInvite> invites = inviteRepository.findByEmailAndStatus(email, InviteStatus.PENDING);
        return invites.stream()
                .map(this::convertToInviteDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void handleInviteResponse(Long inviteId, String userEmail, boolean accepted) {
        EnterpriseInvite invite = inviteRepository.findById(inviteId)
                .orElseThrow(() -> new ResourceNotFoundException("Invite not found"));

        // Verify the invite is for this user and is in PENDING status
        if (!invite.getEmail().equals(userEmail)) {
            throw new AccessDeniedException("Not authorized to handle this invite");
        }

        if (invite.getStatus() != InviteStatus.PENDING) {
            throw new IllegalStateException("Invite is no longer pending");
        }

        try {
            if (accepted) {
                User user = userRepository.findByEmailAndActive(userEmail, true)
                        .orElseThrow(() -> new ResourceNotFoundException("User not found"));

                Enterprise enterprise = enterpriseRepository.findById(invite.getEnterpriseId())
                        .orElseThrow(() -> new ResourceNotFoundException("Enterprise not found"));

                // Check if user is already in an enterprise
                if (user.getEnterprise() != null) {
                    throw new IllegalStateException("User is already part of an enterprise");
                }

                // Find role by name
                Role role = roleRepository.findByName(invite.getRole())
                        .orElseThrow(() -> new ResourceNotFoundException("Role not found"));

                // Update user
                user.setEnterprise(enterprise);
                user.setRole(role);
                userRepository.save(user);

                // Update invite
                invite.setStatus(InviteStatus.ACCEPTED);
            } else {
                invite.setStatus(InviteStatus.DECLINED);
            }
            inviteRepository.save(invite);

        } catch (Exception e) {
            throw new IllegalStateException("Failed to process invite: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void createInviteByEmail(Long enterpriseId, String email, RoleName role, Long inviterId) {
        Enterprise enterprise = enterpriseRepository.findById(enterpriseId)
                .orElseThrow(() -> new ResourceNotFoundException("Enterprise not found"));

        // Check if user with this email already exists in the enterprise
        Optional<User> existingUser = userRepository.findByEmailAndActive(email, true);
        if (existingUser.isPresent() && existingUser.get().getEnterprise() != null) {
            throw new IllegalArgumentException("User is already part of an enterprise");
        }

        // Check if invite already exists
        if (inviteRepository.existsByEmailAndEnterpriseIdAndStatus(email, enterpriseId, InviteStatus.PENDING)) {
            throw new IllegalArgumentException("Invite already exists for this email");
        }

        EnterpriseInvite invite = new EnterpriseInvite();
        invite.setEmail(email);
        invite.setEnterpriseId(enterpriseId);
        invite.setInviterId(inviterId);
        invite.setRole(role);
        invite.setStatus(InviteStatus.PENDING);

        inviteRepository.save(invite);
    }

    public List<DepartmentDTO> getEnterpriseDepartments(Long enterpriseId) {
        Enterprise enterprise = enterpriseRepository.findById(enterpriseId)
                .orElseThrow(() -> new ResourceNotFoundException("Enterprise not found"));

        return departmentRepository.findByEnterpriseId(enterpriseId)
                .stream()
                .map(this::convertToDepartmentDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDTO> getEnterpriseEmployees(Long enterpriseId) {
        log.debug("Fetching employees for enterprise ID: {}", enterpriseId);
        
        List<User> employees = userRepository.findByEnterpriseId(enterpriseId);
        log.debug("Found {} employees", employees.size());
        
        List<UserDTO> dtos = employees.stream()
                .map(user -> {
                    log.debug("Mapping user: {} with role: {}", user.getUsername(), user.getRole().getName());
                    return UserDTO.builder()
                            .id(user.getId())
                            .username(user.getUsername())
                            .email(user.getEmail())
                            .fullName(user.getFullName())
                            .roleName(user.getRole().getName().name())
                            .active(user.getActive())
                            .enterpriseId(user.getEnterprise().getId())
                            .managerId(user.getManager() != null ? user.getManager().getId() : null)
                            .build();
                })
                .collect(Collectors.toList());
        
        log.debug("Returning {} employee DTOs", dtos.size());
        return dtos;
    }

    public DepartmentDTO createDepartment(Long enterpriseId, DepartmentRequest request) {
        Enterprise enterprise = enterpriseRepository.findById(enterpriseId)
                .orElseThrow(() -> new ResourceNotFoundException("Enterprise not found"));

        // Check if department name already exists in this enterprise
        if (departmentRepository.existsByNameAndEnterpriseId(request.getName(), enterpriseId)) {
            throw new IllegalArgumentException("Department with this name already exists in the enterprise");
        }

        Department department = new Department();
        department.setName(request.getName());
        department.setDescription(request.getDescription());
        department.setEnterprise(enterprise);

        // If manager ID is provided, assign the manager
        if (request.getManagerId() != null) {
            User manager = userRepository.findById(request.getManagerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Manager not found"));

            // Verify manager belongs to the same enterprise
            if (!enterpriseId.equals(manager.getEnterprise().getId())) {
                throw new IllegalArgumentException("Manager must belong to the same enterprise");
            }

            department.setManager(manager);
        }

        Department savedDepartment = departmentRepository.save(department);
        return convertToDepartmentDTO(savedDepartment);
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
                .roleName(user.getRole().getName().name())
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
            dto.setTotalEmployees(enterprise.getEmployees().size());
        } else {
            dto.setTotalEmployees(0);
        }
        dto.setEmployeeIds(employeeIds);

        return dto;
    }

    private DepartmentDTO convertToDepartmentDTO(Department department) {
        Set<Long> employeeIds = department.getEmployees().stream()
                .map(User::getId)
                .collect(Collectors.toSet());

        return DepartmentDTO.builder()
                .id(department.getId())
                .name(department.getName())
                .description(department.getDescription())
                .managerName(department.getManager() != null ? department.getManager().getFullName() : null)
                .managerId(department.getManager() != null ? department.getManager().getId() : null)
                .employeeCount(department.getEmployees().size())
                .itemCount(department.getItems().size())
                .employeeIds(employeeIds)
                .build();
    }

    @Override
    public DepartmentDTO assignDepartmentManager(Long departmentId, Long userId, Long enterpriseId) {
        Department department = departmentRepository.findByIdAndEnterpriseId(departmentId, enterpriseId)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found"));

        User manager = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Check if user is an enterprise owner
        if (manager.getRole().getName() == RoleName.ROLE_ENTERPRISE_OWNER) {
            throw new IllegalArgumentException("Enterprise owners cannot be assigned as department managers");
        }

        // Check if user is already a manager of another department
        if (!departmentRepository.findByManagerId(userId).isEmpty()) {
            throw new IllegalArgumentException("User is already assigned as a manager to another department");
        }

        // Get the manager role
        Role managerRole = roleRepository.findByName(RoleName.ROLE_MANAGER)
                .orElseThrow(() -> new ResourceNotFoundException("Manager role not found"));

        // Set the manager's role
        manager.setRole(managerRole);
        manager.setDepartment(department);
        userRepository.save(manager);

        // Set the department manager
        department.setManager(manager);
        Department savedDepartment = departmentRepository.save(department);
        
        return DepartmentDTO.builder()
                .id(savedDepartment.getId())
                .name(savedDepartment.getName())
                .description(savedDepartment.getDescription())
                .enterpriseId(savedDepartment.getEnterprise().getId())
                .enterpriseName(savedDepartment.getEnterprise().getName())
                .managerId(savedDepartment.getManager() != null ? savedDepartment.getManager().getId() : null)
                .managerName(savedDepartment.getManager() != null ? savedDepartment.getManager().getFullName() : null)
                .employeeCount(savedDepartment.getEmployees().size())
                .itemCount(savedDepartment.getItems().size())
                .build();
    }
}