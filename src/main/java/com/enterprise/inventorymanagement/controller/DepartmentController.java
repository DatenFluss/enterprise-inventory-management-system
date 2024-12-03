package com.enterprise.inventorymanagement.controller;

import com.enterprise.inventorymanagement.exceptions.ResourceNotFoundException;
import com.enterprise.inventorymanagement.model.Department;
import com.enterprise.inventorymanagement.model.User;
import com.enterprise.inventorymanagement.model.dto.DepartmentDTO;
import com.enterprise.inventorymanagement.model.dto.EnterpriseDTO;
import com.enterprise.inventorymanagement.model.dto.UserDTO;
import com.enterprise.inventorymanagement.repository.UserRepository;
import com.enterprise.inventorymanagement.service.DepartmentService;
import com.enterprise.inventorymanagement.service.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/departments")
public class DepartmentController {

    private final UserRepository userRepository;
    private final DepartmentService departmentService;

    @Autowired
    public DepartmentController(UserRepository userRepository, DepartmentService departmentService) {
        this.userRepository = userRepository;
        this.departmentService = departmentService;
    }

    @GetMapping("/{departmentId}")
    public ResponseEntity<DepartmentDTO> getDepartmentById(
            @PathVariable Long departmentId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Department department = departmentService.getDepartmentById(departmentId);
        return ResponseEntity.ok(convertToDTO(department));
    }

    @GetMapping("/available-employees")
    @PreAuthorize("hasAuthority('MANAGE_DEPARTMENT')")
    public ResponseEntity<List<UserDTO>> getAvailableEmployees(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        List<User> availableEmployees = userRepository.findAvailableEmployees(userDetails.getEnterpriseId());
        List<UserDTO> employeeDTOs = availableEmployees.stream()
            .map(user -> UserDTO.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .username(user.getUsername())
                .email(user.getEmail())
                .roleName(user.getRole().getName().toString())
                .active(user.getActive())
                .enterpriseId(user.getEnterpriseId())
                .managerId(user.getManagerId())
                .build())
            .collect(Collectors.toList());
        return ResponseEntity.ok(employeeDTOs);
    }

    @PostMapping("/{departmentId}/invite")
    @PreAuthorize("hasAuthority('MANAGE_DEPARTMENT')")
    public ResponseEntity<?> inviteEmployee(
            @PathVariable Long departmentId,
            @RequestBody Map<String, Long> request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        try {
            Long userId = request.get("userId");
            if (userId == null) {
                return ResponseEntity.badRequest().body("User ID is required");
            }

            departmentService.addEmployeeToDepartment(departmentId, userId, userDetails.getEnterpriseId());
            return ResponseEntity.ok().body("Employee added to department successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/my")
    @PreAuthorize("hasAuthority('MANAGE_DEPARTMENT')")
    public ResponseEntity<EnterpriseDTO> getMyDepartment(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        Department department = user.getDepartment();
        if (department == null) {
            throw new ResourceNotFoundException("Department not found for user");
        }
        
        EnterpriseDTO enterpriseDTO = new EnterpriseDTO();
        enterpriseDTO.setId(department.getEnterprise().getId());
        enterpriseDTO.setName(department.getEnterprise().getName());
        enterpriseDTO.setUserDepartment(convertToDTO(department));
        
        return ResponseEntity.ok(enterpriseDTO);
    }

    @GetMapping("/{departmentId}/employees")
    @PreAuthorize("hasAuthority('VIEW_INVENTORY')")
    public ResponseEntity<List<UserDTO>> getDepartmentEmployees(
            @PathVariable Long departmentId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        List<User> employees = departmentService.getDepartmentEmployeesWithData(departmentId);
        Department department = departmentService.getDepartmentById(departmentId);
        
        List<UserDTO> employeeDTOs = employees.stream()
            .map(user -> UserDTO.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .username(user.getUsername())
                .email(user.getEmail())
                .roleName(user.getRole().getName().toString())
                .active(user.getActive())
                .enterpriseId(user.getEnterpriseId())
                .managerId(user.getManagerId())
                .departmentId(departmentId)
                .departmentName(department.getName())
                .createdAt(user.getCreatedAt())
                .build())
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(employeeDTOs);
    }

    private DepartmentDTO convertToDTO(Department department) {
        return DepartmentDTO.builder()
                .id(department.getId())
                .name(department.getName())
                .description(department.getDescription())
                .enterpriseId(department.getEnterprise().getId())
                .enterpriseName(department.getEnterprise().getName())
                .managerId(department.getManager() != null ? department.getManager().getId() : null)
                .managerName(department.getManager() != null ? department.getManager().getFullName() : null)
                .employeeCount(department.getEmployees() != null ? department.getEmployees().size() : 0)
                .itemCount(department.getItems() != null ? 
                        department.getItems().stream()
                                .mapToInt(item -> item.getQuantity())
                                .sum() : 0)
                .employeeIds(department.getEmployees() != null ? 
                        department.getEmployees().stream()
                                .map(User::getId)
                                .collect(Collectors.toSet()) : 
                        Collections.emptySet())
                .build();
    }
} 