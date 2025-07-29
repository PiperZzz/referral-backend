package org.moyi_tech.usermanagement.entity;

public enum RoleName {
    ROLE_USER("普通用户"),
    ROLE_ADMIN("管理员"),
    ROLE_MASTER("超级管理员");

    private final String description;

    RoleName(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}