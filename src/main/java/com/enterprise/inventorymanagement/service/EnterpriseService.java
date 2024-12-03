package com.enterprise.inventorymanagement.service;

import com.enterprise.inventorymanagement.exceptions.ResourceNotFoundException;
import com.enterprise.inventorymanagement.model.RoleName;
import com.enterprise.inventorymanagement.model.dto.DepartmentDTO;
import com.enterprise.inventorymanagement.model.dto.EnterpriseDTO;
import com.enterprise.inventorymanagement.model.dto.EnterpriseInviteDTO;
import com.enterprise.inventorymanagement.model.dto.UserDTO;
import com.enterprise.inventorymanagement.model.dto.WarehouseDTO;
import com.enterprise.inventorymanagement.model.request.DepartmentRequest;
import com.enterprise.inventorymanagement.model.request.EnterpriseRegistrationRequest;

import java.util.List;

/**
 * Service interface for enterprise-related operations.
 */
public interface EnterpriseService {

    /**
     * Registers a new enterprise along with its owner.
     *
     * @param request The enterprise registration request containing enterprise and owner details.
     */
    void registerEnterprise(EnterpriseRegistrationRequest request);

    /**
     * Retrieves an enterprise by its ID.
     *
     * @param enterpriseId The ID of the enterprise.
     * @return The enterprise with the specified ID.
     * @throws ResourceNotFoundException if the enterprise is not found.
     */
    EnterpriseDTO getEnterpriseById(Long enterpriseId) throws ResourceNotFoundException;

    /**
     * Retrieves all enterprises.
     *
     * @return A list of all enterprises.
     */
    List<EnterpriseDTO> getAllEnterprises();

    /**
     * Updates the details of an existing enterprise.
     *
     * @param enterpriseId The ID of the enterprise to update.
     * @param updatedEnterprise The updated enterprise details.
     * @return The updated enterprise.
     * @throws ResourceNotFoundException if the enterprise is not found.
     */
    EnterpriseDTO updateEnterprise(Long enterpriseId, EnterpriseDTO updatedEnterprise) throws ResourceNotFoundException;

    /**
     * Deletes an enterprise by its ID.
     *
     * @param enterpriseId The ID of the enterprise to delete.
     * @throws ResourceNotFoundException if the enterprise is not found.
     */
    void deleteEnterprise(Long enterpriseId) throws ResourceNotFoundException;

    /**
     * Adds an employee to an enterprise.
     *
     * @param enterpriseId The ID of the enterprise.
     * @param employeeId The ID of the employee.
     * @throws ResourceNotFoundException if the enterprise or employee is not found.
     */
    void addEmployeeToEnterprise(Long enterpriseId, Long employeeId) throws ResourceNotFoundException;

    /**
     * Removes an employee from an enterprise.
     *
     * @param enterpriseId The ID of the enterprise.
     * @param employeeId The ID of the employee.
     * @throws ResourceNotFoundException if the enterprise or employee is not found.
     */
    void removeEmployeeFromEnterprise(Long enterpriseId, Long employeeId) throws ResourceNotFoundException;

    List<EnterpriseInviteDTO> getInvitesForUser(String email);
    void createInviteByEmail(Long enterpriseId, String email, RoleName role, Long inviterId);
    void handleInviteResponse(Long inviteId, String userEmail, boolean accepted);
    List<UserDTO> getEnterpriseEmployees(Long enterpriseId);
    List<DepartmentDTO> getEnterpriseDepartments(Long enterpriseId);
    DepartmentDTO createDepartment(Long enterpriseId, DepartmentRequest request);
    DepartmentDTO assignDepartmentManager(Long departmentId, Long userId, Long enterpriseId);
    List<WarehouseDTO> getWarehousesByEnterpriseId(Long enterpriseId);
}

