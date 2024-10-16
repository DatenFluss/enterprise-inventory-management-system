package com.enterprise.inventorymanagement.model.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;

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

}
