package org.moyi_tech.usermanagement.dto;

import org.moyi_tech.usermanagement.util.RoleUtils;

public class LoginResponse {
    private String token;
    private String type = "Bearer";
    private UserResponseDto user;
    private String primaryRole;    // "MASTER", "ADMIN", "USER"
    private String defaultRoute;   // 默认路由

    public LoginResponse(String token, UserResponseDto user) {
        this.token = token;
        this.user = user;
        // 如果没有提供角色信息，从用户DTO中推断
        this.primaryRole = RoleUtils.determinePrimaryRole(user.getRoles());
        this.defaultRoute = RoleUtils.getDefaultRouteForRole(this.primaryRole);
    }

    public LoginResponse(String token, UserResponseDto user, String primaryRole, String defaultRoute) {
        this.token = token;
        this.user = user;
        this.primaryRole = primaryRole;
        this.defaultRoute = defaultRoute;
    }

    // Getters and Setters
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public UserResponseDto getUser() { return user; }
    public void setUser(UserResponseDto user) { this.user = user; }

    public String getPrimaryRole() { return primaryRole; }
    public void setPrimaryRole(String primaryRole) { this.primaryRole = primaryRole; }

    public String getDefaultRoute() { return defaultRoute; }
    public void setDefaultRoute(String defaultRoute) { this.defaultRoute = defaultRoute; }
}
