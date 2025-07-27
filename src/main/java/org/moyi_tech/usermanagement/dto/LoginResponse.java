package org.moyi_tech.usermanagement.dto;

public class LoginResponse {
    private String token;
    private String type = "Bearer";
    private UserResponseDto user;

    public LoginResponse(String token, UserResponseDto user) {
        this.token = token;
        this.user = user;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public UserResponseDto getUser() { return user; }
    public void setUser(UserResponseDto user) { this.user = user; }
}
