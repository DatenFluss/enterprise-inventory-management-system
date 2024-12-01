package com.enterprise.inventorymanagement.model.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemRequestDTO {
    private Long id;
    
    @NotNull(message = "Source warehouse ID is required")
    private Long warehouseId;
    private String warehouseName;
    
    @NotNull(message = "Target department ID is required")
    private Long departmentId;
    private String departmentName;
    
    @NotEmpty(message = "Request must contain at least one item")
    @Valid
    private List<RequestItemDTO> requestItems;
    
    private String status;
    private String comments;
    private String responseComments;
    
    private Long requesterId;
    private String requesterName;
    
    private Long processorId;
    private String processorName;
    
    private LocalDateTime requestDate;
    private LocalDateTime processedDate;
}

