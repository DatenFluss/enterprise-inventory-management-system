package com.enterprise.inventorymanagement.model.dto;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentDTO {
    private Long id;
    private String name;
    private String description;
    private Long enterpriseId;
    private String enterpriseName;
    private Long managerId;
    private String managerName;
    private int employeeCount;
    private int itemCount;
    private Set<Long> employeeIds;
}