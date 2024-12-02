package com.enterprise.inventorymanagement.model.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;
import java.util.List;

/**
 * DTO for transferring enterprise data.
 */
@Getter
@Setter
public class EnterpriseDTO {

    private Long id;
    private String name;
    private String address;
    private String contactEmail;
    private Set<Long> employeeIds;
    private int totalEmployees;
    private List<DepartmentDTO> departments;
    private DepartmentDTO userDepartment; // Department where the current user belongs
}
