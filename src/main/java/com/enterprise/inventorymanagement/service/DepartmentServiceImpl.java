package com.enterprise.inventorymanagement.service;

import com.enterprise.inventorymanagement.exceptions.ResourceNotFoundException;
import com.enterprise.inventorymanagement.model.Department;
import com.enterprise.inventorymanagement.model.Enterprise;
import com.enterprise.inventorymanagement.model.User;
import com.enterprise.inventorymanagement.model.dto.DepartmentDTO;
import com.enterprise.inventorymanagement.repository.DepartmentRepository;
import com.enterprise.inventorymanagement.repository.EnterpriseRepository;
import com.enterprise.inventorymanagement.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final EnterpriseRepository enterpriseRepository;
    private final UserRepository userRepository;

    @Autowired
    public DepartmentServiceImpl(DepartmentRepository departmentRepository,
                                 EnterpriseRepository enterpriseRepository,
                                 UserRepository userRepository) {
        this.departmentRepository = departmentRepository;
        this.enterpriseRepository = enterpriseRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Department createDepartment(Long enterpriseId, Department department) {
        Enterprise enterprise = enterpriseRepository.findById(enterpriseId)
                .orElseThrow(() -> new ResourceNotFoundException("Enterprise not found with id: " + enterpriseId));

        if (departmentRepository.existsByNameAndEnterpriseId(department.getName(), enterpriseId)) {
            throw new IllegalArgumentException("Department with name '" + department.getName() +
                    "' already exists in this enterprise");
        }

        department.setEnterprise(enterprise);
        return departmentRepository.save(department);
    }

    @Override
    public Department updateDepartment(Long departmentId, Department departmentDetails) {
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + departmentId));

        // Check if new name doesn't conflict with existing departments in the same enterprise
        if (!department.getName().equals(departmentDetails.getName()) &&
                departmentRepository.existsByNameAndEnterpriseId(
                        departmentDetails.getName(),
                        department.getEnterprise().getId())) {
            throw new IllegalArgumentException("Department with name '" + departmentDetails.getName() +
                    "' already exists in this enterprise");
        }

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

    private DepartmentDTO convertToDTO(Department department) {
        return DepartmentDTO.builder()
                .id(department.getId())
                .name(department.getName())
                .managerName(department.getManager() != null ? department.getManager().getFullName() : null)
                .employeeCount(department.getEmployees().size())
                .itemCount(department.getItems().size())
                .description(department.getDescription())
                .build();
    }
}
