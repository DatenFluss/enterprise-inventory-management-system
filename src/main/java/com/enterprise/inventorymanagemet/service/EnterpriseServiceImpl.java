package com.enterprise.inventorymanagemet.service;

import com.enterprise.inventorymanagemet.exceptions.ResourceNotFoundException;
import com.enterprise.inventorymanagemet.model.Enterprise;
import com.enterprise.inventorymanagemet.model.RoleName;
import com.enterprise.inventorymanagemet.model.User;
import com.enterprise.inventorymanagemet.model.dto.EnterpriseDTO;
import com.enterprise.inventorymanagemet.repository.EnterpriseRepository;
import com.enterprise.inventorymanagemet.repository.InventoryItemRepository;
import com.enterprise.inventorymanagemet.repository.RoleRepository;
import com.enterprise.inventorymanagemet.repository.UserRepository;
import com.enterprise.inventorymanagemet.model.request.EnterpriseRegistrationRequest;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    public EnterpriseServiceImpl(
            UserRepository userRepository,
            InventoryItemRepository itemRepository,
            RoleRepository roleRepository,
            EnterpriseRepository enterpriseRepository,
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
    }

    @Override
    @Transactional
    public void registerEnterprise(EnterpriseRegistrationRequest request) {
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
        owner.setIsActive(true);
        owner.setEnterprise(savedEnterprise);

        owner.setRole(roleRepository.findByName(RoleName.ENTERPRISE_OWNER).orElseThrow());

        userRepository.save(owner);
    }

    @Override
    public EnterpriseDTO getEnterpriseById(Long enterpriseId) throws ResourceNotFoundException {
        Enterprise enterprise = enterpriseRepository.findById(enterpriseId)
                .orElseThrow(() -> new ResourceNotFoundException("Enterprise not found with ID: " + enterpriseId));

        return convertToDTO(enterprise);
    }

    @Override
    public List<EnterpriseDTO> getAllEnterprises() {
        List<Enterprise> enterprises = enterpriseRepository.findAll();
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

    // Helper method to convert Enterprise to EnterpriseDTO
    private EnterpriseDTO convertToDTO(Enterprise enterprise) {
        EnterpriseDTO dto = new EnterpriseDTO();
        dto.setId(enterprise.getId());
        dto.setName(enterprise.getName());
        dto.setAddress(enterprise.getAddress());
        dto.setContactEmail(enterprise.getContactEmail());

        Set<Long> employeeIds = enterprise.getEmployees().stream()
                .map(User::getId)
                .collect(Collectors.toSet());
        dto.setEmployeeIds(employeeIds);

        return dto;
    }
}
