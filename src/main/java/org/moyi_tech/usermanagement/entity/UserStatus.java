package org.moyi_tech.usermanagement.entity;

public enum UserStatus {
    ACTIVE("Active"),
    INACTIVE("Inactive");

    private final String description;

    UserStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}