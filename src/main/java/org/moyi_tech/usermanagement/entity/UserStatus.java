package org.moyi_tech.usermanagement.entity;

public enum UserStatus {
    ACTIVE("激活"),
    INACTIVE("未激活"),
    SUSPENDED("暂停");

    private final String description;

    UserStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}