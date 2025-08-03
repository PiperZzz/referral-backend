package org.moyi_tech.usermanagement.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class PasswordResetRequest {
    @Email(message = "请输入有效的邮箱地址")
    @NotBlank(message = "邮箱不能为空")
    private String email;

    @NotBlank(message = "重置Token不能为空")
    private String token;

    @NotBlank(message = "新密码不能为空")
    @Size(min = 8, max = 20, message = "密码长度必须在8-20字符之间")
    private String newPassword;

    public PasswordResetRequest() {}

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
}
