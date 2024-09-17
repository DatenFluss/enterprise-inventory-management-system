package com.enterprise.inventorymanagemet.service.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO for enterprise registration requests.
 */

@Getter
@Setter
public class EnterpriseRegistrationRequest {

    @NotBlank
    private String enterpriseName;

    private String address;

    @Email
    private String contactEmail;

    @NotBlank
    private String ownerUsername;

    @NotBlank
    private String ownerPassword;

    @Email
    private String ownerEmail;

}
