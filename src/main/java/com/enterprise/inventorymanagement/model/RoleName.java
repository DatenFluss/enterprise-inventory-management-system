package com.enterprise.inventorymanagement.model;

public enum RoleName {
    ADMIN ("ENTERPRISE_OWNER"),
    ENTERPRISE_OWNER ("ENTERPRISE_OWNER"),
    ENTERPRISE_ADMINISTRATOR ("ENTERPRISE_OWNER"),
    ENTERPRISE_MANAGER ("ENTERPRISE_OWNER"),
    EMPLOYEE ("ENTERPRISE_OWNER");

    public final String label;

    private RoleName(String label) {
        this.label = label;
    }
}
