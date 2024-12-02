package com.enterprise.inventorymanagement.model.dto;

import com.enterprise.inventorymanagement.model.request.RequestStatus;
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
    private Long requesterId;
    private String requesterName;
    private Long warehouseId;
    private String warehouseName;
    private Long departmentId;
    private String departmentName;
    private RequestStatus status;
    private LocalDateTime requestDate;
    private LocalDateTime processedDate;
    private String comments;
    private String responseComments;
    private Long processorId;
    private String processorName;
    private List<RequestItemDTO> requestItems;
}

