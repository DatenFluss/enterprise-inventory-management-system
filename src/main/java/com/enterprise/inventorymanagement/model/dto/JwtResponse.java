package com.enterprise.inventorymanagement.model.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JwtResponse {

    private String token;
    private String type = "Bearer";
    private Long id;
    private String username;
    private String email;
    private Long enterpriseId;
    private String role;

    public JwtResponse(String token) {
        this.token = token;
    }

    public JwtResponse(String token, Long id, String username, String email, Long enterpriseId, String role) {
        this.token = token;
        this.id = id;
        this.username = username;
        this.email = email;
        this.enterpriseId = enterpriseId;
        this.role = role;
    }
}
