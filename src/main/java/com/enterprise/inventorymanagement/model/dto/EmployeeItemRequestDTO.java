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
public class EmployeeItemRequestDTO {
    private Long id;
    private Long requesterId;
    private String requesterName;
    private Long departmentId;
    private String departmentName;
    private RequestStatus status;
    private String comments;
    private String responseComments;
    private LocalDateTime requestDate;
    private LocalDateTime processedDate;
    private Long processorId;
    private String processorName;
    private List<RequestItemDTO> requestItems;
} 