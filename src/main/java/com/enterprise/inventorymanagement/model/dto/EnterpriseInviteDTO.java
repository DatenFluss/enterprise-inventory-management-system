package com.enterprise.inventorymanagement.model.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EnterpriseInviteDTO {
    private Long id;
    private String enterpriseName;
    private String role;
    private String inviterName;
    private LocalDateTime createdAt;
}
