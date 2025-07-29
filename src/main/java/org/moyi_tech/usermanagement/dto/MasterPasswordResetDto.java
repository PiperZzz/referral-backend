package org.moyi_tech.usermanagement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class MasterPasswordResetDto {
    @NotBlank(message = "重置Token不能为空")
    private String token;

    @NotBlank(message = "新密码不能为空")
    @Size(min = 8, max = 20, message = "密码长度必须在8-20字符之间")
    private String newPassword;

    public MasterPasswordResetDto() {}

    // Getters and Setters
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
}
