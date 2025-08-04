package org.moyi_tech.usermanagement.dto;

import org.moyi_tech.usermanagement.util.RoleUtils;

public class LoginResponse {
    private String token;
    private String type = "Bearer";
    private UserInfoDto user;
    private String primaryRole;    // "MASTER", "ADMIN", "USER"
    private String defaultRoute;

    public LoginResponse(String token, UserInfoDto user) {
        this.token = token;
        this.user = user;
        // Determine primary role and default route based on user roles
        this.primaryRole = RoleUtils.determinePrimaryRole(user.getRoles());
        this.defaultRoute = RoleUtils.getDefaultRouteForRole(this.primaryRole);
    }

    public LoginResponse(String token, UserInfoDto user, String primaryRole, String defaultRoute) {
        this.token = token;
        this.user = user;
        this.primaryRole = primaryRole;
        this.defaultRoute = defaultRoute;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public UserInfoDto getUser() { return user; }
    public void setUser(UserInfoDto user) { this.user = user; }

    public String getPrimaryRole() { return primaryRole; }
    public void setPrimaryRole(String primaryRole) { this.primaryRole = primaryRole; }

    public String getDefaultRoute() { return defaultRoute; }
    public void setDefaultRoute(String defaultRoute) { this.defaultRoute = defaultRoute; }
}
