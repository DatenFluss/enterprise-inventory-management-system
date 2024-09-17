package com.enterprise.inventorymanagemet.service;

import com.enterprise.inventorymanagemet.exceptions.ResourceNotFoundException;
import com.enterprise.inventorymanagemet.model.dto.EnterpriseDTO;
import com.enterprise.inventorymanagemet.service.requests.EnterpriseRegistrationRequest;

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
}

