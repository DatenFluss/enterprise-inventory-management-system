package com.enterprise.inventorymanagement.service;

import com.enterprise.inventorymanagement.exceptions.ResourceNotFoundException;
import com.enterprise.inventorymanagement.model.Department;
import com.enterprise.inventorymanagement.model.Enterprise;
import com.enterprise.inventorymanagement.model.RoleName;
import com.enterprise.inventorymanagement.model.User;
import com.enterprise.inventorymanagement.model.dto.DepartmentDTO;
import com.enterprise.inventorymanagement.repository.DepartmentRepository;
import com.enterprise.inventorymanagement.repository.EnterpriseRepository;
import com.enterprise.inventorymanagement.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final EnterpriseRepository enterpriseRepository;
    private final UserRepository userRepository;

    @Autowired
    public DepartmentServiceImpl(
            DepartmentRepository departmentRepository,
            EnterpriseRepository enterpriseRepository,
            UserRepository userRepository) {
        this.departmentRepository = departmentRepository;
        this.enterpriseRepository = enterpriseRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Department getDepartmentById(Long departmentId) {
        return departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + departmentId));
    }

    @Override
    public Department createDepartment(Long enterpriseId, Department department) {
        Enterprise enterprise = enterpriseRepository.findById(enterpriseId)
                .orElseThrow(() -> new ResourceNotFoundException("Enterprise not found with id: " + enterpriseId));

        department.setEnterprise(enterprise);
        return departmentRepository.save(department);
    }

    @Override
    public Department updateDepartment(Long departmentId, Department departmentDetails) {
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + departmentId));

        department.setName(departmentDetails.getName());
        department.setDescription(departmentDetails.getDescription());

        return departmentRepository.save(department);
    }

    @Override
    public void deleteDepartment(Long departmentId) {
        if (!departmentRepository.existsById(departmentId)) {
            throw new ResourceNotFoundException("Department not found with id: " + departmentId);
        }

        Department department = departmentRepository.findById(departmentId).get();

        // Check if department has employees
        if (!department.getEmployees().isEmpty()) {
            throw new IllegalStateException("Cannot delete department that has employees");
        }

        // Check if department has items
        if (!department.getItems().isEmpty()) {
            throw new IllegalStateException("Cannot delete department that has items");
        }

        departmentRepository.deleteById(departmentId);
    }

    @Override
    public Department assignManager(Long departmentId, Long userId) {
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + departmentId));

        User manager = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Verify user belongs to the same enterprise
        if (!manager.getEnterprise().getId().equals(department.getEnterprise().getId())) {
            throw new IllegalArgumentException("Manager must belong to the same enterprise");
        }

        department.setManager(manager);
        return departmentRepository.save(department);
    }

    @Override
    public List<DepartmentDTO> getDepartmentsByEnterpriseId(Long enterpriseId) {
        if (!enterpriseRepository.existsById(enterpriseId)) {
            throw new ResourceNotFoundException("Enterprise not found with id: " + enterpriseId);
        }

        return departmentRepository.findByEnterpriseId(enterpriseId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void addEmployeeToDepartment(Long departmentId, Long userId, Long enterpriseId) {
        Department department = departmentRepository.findById(departmentId)
            .orElseThrow(() -> new ResourceNotFoundException("Department not found"));

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Verify department belongs to the enterprise
        if (!department.getEnterprise().getId().equals(enterpriseId)) {
            throw new IllegalArgumentException("Department does not belong to the enterprise");
        }

        // Verify user belongs to the enterprise
        if (!user.getEnterprise().getId().equals(enterpriseId)) {
            throw new IllegalArgumentException("User does not belong to the enterprise");
        }

        // Check if user is already in a department
        if (user.getDepartment() != null) {
            throw new IllegalArgumentException("User is already assigned to a department");
        }

        // Update both sides of the relationship
        user.setDepartment(department);
        department.getEmployees().add(user);

        // Set the department manager as the user's manager if available
        if (department.getManager() != null) {
            user.setManager(department.getManager());
            department.getManager().getSubordinates().add(user);
            userRepository.save(department.getManager());
        }

        // Save both entities to persist the relationship
        departmentRepository.save(department);
        userRepository.save(user);

        // Refresh the user to ensure all relationships are properly loaded
        user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getDepartmentEmployeesWithData(Long departmentId) throws ResourceNotFoundException {
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + departmentId));
        
        return department.getEmployees().stream()
                .map(user -> userRepository.findByIdWithDepartment(user.getId())
                        .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + user.getId())))
                .collect(Collectors.toList());
    }

    private DepartmentDTO convertToDTO(Department department) {
        int totalItemCount = department.getItems() != null ? 
            department.getItems().stream()
                .mapToInt(item -> item.getQuantity() != null ? item.getQuantity() : 0)
                .sum() : 0;

        return DepartmentDTO.builder()
                .id(department.getId())
                .name(department.getName())
                .description(department.getDescription())
                .enterpriseId(department.getEnterprise().getId())
                .enterpriseName(department.getEnterprise().getName())
                .managerId(department.getManager() != null ? department.getManager().getId() : null)
                .managerName(department.getManager() != null ? department.getManager().getFullName() : null)
                .employeeCount(department.getEmployees() != null ? department.getEmployees().size() : 0)
                .itemCount(totalItemCount)
                .employeeIds(department.getEmployees() != null ? 
                        department.getEmployees().stream()
                                .map(User::getId)
                                .collect(Collectors.toSet()) : 
                        Collections.emptySet())
                .build();
    }
}
