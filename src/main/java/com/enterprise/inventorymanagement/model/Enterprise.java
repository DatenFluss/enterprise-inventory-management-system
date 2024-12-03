package com.enterprise.inventorymanagement.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Table(name = "enterprises")
public class Enterprise {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Company name is required")
    @Size(min = 3, max = 100, message = "Company name must be between 3 and 100 characters")
    private String name;

    @NotBlank(message = "Company name is required")
    @Size(min = 3, max = 100, message = "Company name must be between 3 and 100 characters")
    private String address;

    @Column(nullable = false, unique = true, length = 100)
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String contactEmail;

    @OneToMany(mappedBy = "enterprise", fetch = FetchType.EAGER)
    private Set<User> employees = new HashSet<>();

    @OneToMany(mappedBy = "enterprise", fetch = FetchType.EAGER)
    private List<Department> departments = new ArrayList<>();

    // Helper method to avoid circular loading
    public Long getId() {
        return id;
    }

    // Helper method to safely get employee count
    public int getEmployeeCount() {
        return employees != null ? employees.size() : 0;
    }
}
