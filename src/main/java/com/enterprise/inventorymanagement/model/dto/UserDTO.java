package com.enterprise.inventorymanagement.model.dto;

import com.enterprise.inventorymanagement.model.RoleName;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class UserDTO {
    private Long id;
    private String fullName;
    private String username;
    private String email;
    private Boolean active;
    private String roleName;
    private Long enterpriseId;
    private Long managerId;
    private Long departmentId;
    private String departmentName;
    private LocalDateTime createdAt;
}
