package com.enterprise.inventorymanagement.service;

import com.enterprise.inventorymanagement.exceptions.ResourceNotFoundException;
import com.enterprise.inventorymanagement.model.*;
import com.enterprise.inventorymanagement.model.dto.*;
import com.enterprise.inventorymanagement.model.request.DepartmentRequest;
import com.enterprise.inventorymanagement.model.request.EnterpriseRegistrationRequest;
import com.enterprise.inventorymanagement.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Collections;

@Service
@Transactional
public class EnterpriseServiceImpl implements EnterpriseService {

    private final EnterpriseRepository enterpriseRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final InviteRepository inviteRepository;
    private final DepartmentRepository departmentRepository;
    private final WarehouseService warehouseService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public EnterpriseServiceImpl(
            UserRepository userRepository,
            InventoryItemRepository itemRepository,
            RoleRepository roleRepository,
            EnterpriseRepository enterpriseRepository,
            InviteRepository inviteRepository,
            DepartmentRepository departmentRepository,
            PasswordEncoder passwordEncoder,
            WarehouseService warehouseService) {
        this.enterpriseRepository = enterpriseRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.inviteRepository = inviteRepository;
        this.departmentRepository = departmentRepository;
        this.warehouseService = warehouseService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void registerEnterprise(EnterpriseRegistrationRequest request) {
        // Create enterprise
        Enterprise enterprise = new Enterprise();
        enterprise.setName(request.getEnterpriseName());
        enterprise.setAddress(request.getAddress());
        enterprise.setContactEmail(request.getContactEmail());
        enterprise = enterpriseRepository.save(enterprise);

        // Create owner
        User owner = new User();
        owner.setUsername(request.getOwnerUsername());
        owner.setPassword(passwordEncoder.encode(request.getOwnerPassword()));
        owner.setEmail(request.getOwnerEmail());
        owner.setFullName(request.getOwnerFullName());
        owner.setActive(true);
        owner.setEnterprise(enterprise);

        // Set owner role
        Role ownerRole = roleRepository.findByName(RoleName.ROLE_ENTERPRISE_OWNER)
                .orElseThrow(() -> new ResourceNotFoundException("Owner role not found"));
        owner.setRole(ownerRole);

        userRepository.save(owner);
    }

    @Override
    public EnterpriseDTO getEnterpriseById(Long enterpriseId) throws ResourceNotFoundException {
        Enterprise enterprise = enterpriseRepository.findByIdWithEmployees(enterpriseId)
                .orElseThrow(() -> new ResourceNotFoundException("Enterprise not found with ID: " + enterpriseId));
        return convertToDTO(enterprise);
    }

    @Override
    public List<EnterpriseDTO> getAllEnterprises() {
        return enterpriseRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public EnterpriseDTO updateEnterprise(Long enterpriseId, EnterpriseDTO updatedEnterprise) throws ResourceNotFoundException {
        Enterprise enterprise = enterpriseRepository.findById(enterpriseId)
                .orElseThrow(() -> new ResourceNotFoundException("Enterprise not found with ID: " + enterpriseId));
        enterprise.setName(updatedEnterprise.getName());
        enterprise.setAddress(updatedEnterprise.getAddress());
        enterprise.setContactEmail(updatedEnterprise.getContactEmail());
        return convertToDTO(enterpriseRepository.save(enterprise));
    }

    @Override
    public void deleteEnterprise(Long enterpriseId) throws ResourceNotFoundException {
        if (!enterpriseRepository.existsById(enterpriseId)) {
            throw new ResourceNotFoundException("Enterprise not found with ID: " + enterpriseId);
        }
        enterpriseRepository.deleteById(enterpriseId);
    }

    @Override
    public void addEmployeeToEnterprise(Long enterpriseId, Long employeeId) throws ResourceNotFoundException {
        Enterprise enterprise = enterpriseRepository.findById(enterpriseId)
                .orElseThrow(() -> new ResourceNotFoundException("Enterprise not found with ID: " + enterpriseId));
        User employee = userRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + employeeId));
        employee.setEnterprise(enterprise);
        userRepository.save(employee);
    }

    @Override
    public void removeEmployeeFromEnterprise(Long enterpriseId, Long employeeId) throws ResourceNotFoundException {
        User employee = userRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + employeeId));
        if (employee.getEnterprise() == null || !employee.getEnterprise().getId().equals(enterpriseId)) {
            throw new ResourceNotFoundException("Employee not found in enterprise with ID: " + enterpriseId);
        }
        employee.setEnterprise(null);
        userRepository.save(employee);
    }

    @Override
    public List<EnterpriseInviteDTO> getInvitesForUser(String email) {
        return inviteRepository.findByEmailAndStatus(email, InviteStatus.PENDING).stream()
                .map(this::convertToInviteDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void createInviteByEmail(Long enterpriseId, String email, RoleName role, Long inviterId) {
        EnterpriseInvite invite = new EnterpriseInvite();
        invite.setEmail(email);
        invite.setEnterpriseId(enterpriseId);
        invite.setInviterId(inviterId);
        invite.setRole(role);
        invite.setStatus(InviteStatus.PENDING);
        inviteRepository.save(invite);
    }

    @Override
    public void handleInviteResponse(Long inviteId, String userEmail, boolean accepted) {
        EnterpriseInvite invite = inviteRepository.findById(inviteId)
                .orElseThrow(() -> new ResourceNotFoundException("Invite not found"));
        invite.setStatus(accepted ? InviteStatus.ACCEPTED : InviteStatus.DECLINED);
        inviteRepository.save(invite);
    }

    @Override
    public List<UserDTO> getEnterpriseEmployees(Long enterpriseId) {
        return userRepository.findByEnterpriseId(enterpriseId).stream()
                .map(this::convertToUserDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<DepartmentDTO> getEnterpriseDepartments(Long enterpriseId) {
        return departmentRepository.findByEnterpriseId(enterpriseId).stream()
                .map(this::convertToDepartmentDTO)
                .collect(Collectors.toList());
    }

    @Override
    public DepartmentDTO createDepartment(Long enterpriseId, DepartmentRequest request) {
        Enterprise enterprise = enterpriseRepository.findById(enterpriseId)
                .orElseThrow(() -> new ResourceNotFoundException("Enterprise not found"));
        Department department = new Department();
        department.setName(request.getName());
        department.setDescription(request.getDescription());
        department.setEnterprise(enterprise);
        return convertToDepartmentDTO(departmentRepository.save(department));
    }

    @Override
    public DepartmentDTO assignDepartmentManager(Long departmentId, Long userId, Long enterpriseId) {
        Department department = departmentRepository.findByIdAndEnterpriseId(departmentId, enterpriseId)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found"));
        User manager = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Update user's role to ROLE_MANAGER
        Role managerRole = roleRepository.findByName(RoleName.ROLE_MANAGER)
                .orElseThrow(() -> new ResourceNotFoundException("Manager role not found"));
        manager.setRole(managerRole);
        userRepository.save(manager);

        // Set manager for the department
        department.setManager(manager);
        manager.setDepartment(department);

        // Update manager's subordinates
        department.getEmployees().stream()
                .filter(employee -> !employee.getId().equals(userId))
                .forEach(employee -> {
                    employee.setManager(manager);
                    userRepository.save(employee);
                });

        Department savedDepartment = departmentRepository.save(department);
        return convertToDepartmentDTO(savedDepartment);
    }

    @Override
    public List<WarehouseDTO> getWarehousesByEnterpriseId(Long enterpriseId) {
        return warehouseService.getWarehousesByEnterpriseId(enterpriseId);
    }

    private EnterpriseDTO convertToDTO(Enterprise enterprise) {
        EnterpriseDTO dto = new EnterpriseDTO();
        dto.setId(enterprise.getId());
        dto.setName(enterprise.getName());
        dto.setAddress(enterprise.getAddress());
        dto.setContactEmail(enterprise.getContactEmail());
        dto.setTotalEmployees(enterprise.getEmployeeCount());
        dto.setEmployeeIds(enterprise.getEmployees().stream()
                .map(User::getId)
                .collect(Collectors.toSet()));
        dto.setDepartments(enterprise.getDepartments().stream()
                .map(this::convertToDepartmentDTO)
                .collect(Collectors.toList()));
        return dto;
    }

    private UserDTO convertToUserDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .active(user.getActive())
                .roleName(user.getRole().getName().toString())
                .enterpriseId(user.getEnterpriseId())
                .managerId(user.getManagerId())
                .departmentId(user.getDepartment() != null ? user.getDepartment().getId() : null)
                .departmentName(user.getDepartment() != null ? user.getDepartment().getName() : null)
                .createdAt(user.getCreatedAt())
                .build();
    }

    private DepartmentDTO convertToDepartmentDTO(Department department) {
        return DepartmentDTO.builder()
                .id(department.getId())
                .name(department.getName())
                .description(department.getDescription())
                .enterpriseId(department.getEnterprise().getId())
                .enterpriseName(department.getEnterprise().getName())
                .managerId(department.getManager() != null ? department.getManager().getId() : null)
                .managerName(department.getManager() != null ? department.getManager().getFullName() : null)
                .employeeCount(department.getEmployees() != null ? department.getEmployees().size() : 0)
                .itemCount(department.getItems() != null ? department.getItems().size() : 0)
                .employeeIds(department.getEmployees() != null ? 
                        department.getEmployees().stream()
                                .map(User::getId)
                                .collect(Collectors.toSet()) : 
                        Collections.emptySet())
                .build();
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
}