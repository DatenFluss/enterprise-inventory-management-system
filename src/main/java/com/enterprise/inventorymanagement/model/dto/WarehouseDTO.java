package com.enterprise.inventorymanagement.model.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WarehouseDTO {
    private Long id;
    private String name;
    private String description;
    private String location;
    private Long managerId;
    private Long enterpriseId;
    private String enterpriseName;
    private String managerName;
    private Integer itemCount;
}
