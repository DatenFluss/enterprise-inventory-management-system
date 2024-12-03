package com.enterprise.inventorymanagement.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentInviteDTO {
    private Long id;
    private Long departmentId;
    private String departmentName;
    private Long userId;
    private String userName;
    private String userEmail;
    private String status;
    private LocalDateTime createdAt;
} 