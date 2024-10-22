package com.enterprise.inventorymanagement.model.dto;

import com.enterprise.inventorymanagement.model.RoleName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDTO {
    private Long id;
    private String username;
    private String email;
    private boolean active;
    private String roleName;
}
