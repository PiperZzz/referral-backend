package org.moyi_tech.usermanagement.constant;

public enum RoleName {
    ROLE_USER("User"),
    ROLE_ADMIN("Admin"),
    ROLE_MASTER("Master");

    private final String description;

    RoleName(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}