package com.enterprise.inventorymanagement.model;

public enum RoleName {
    ADMIN ("ADMIN"),
    OWNER("OWNER"),
    MANAGER("MANAGER"),
    EMPLOYEE ("EMPLOYEE"),
    UNAFFILIATED ("UNAFFILIATED");

    public final String label;

    RoleName(String label) {
        this.label = label;
    }
}
