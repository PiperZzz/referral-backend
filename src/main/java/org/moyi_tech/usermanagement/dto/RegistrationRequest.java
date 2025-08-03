package org.moyi_tech.usermanagement.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegistrationRequest {
    
    @Email(message = "Please enter a valid email address")
    @NotBlank(message = "Email cannot be empty")
    private String email;

    @NotBlank(message = "Password cannot be empty")
    @Size(min = 6, max = 20, message = "Password length must be between 6-20 characters")
    private String password;

    private String wechatId;
    
    private String referrerWechatId;

    // 构造函数
    public RegistrationRequest() {}

    public RegistrationRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    // Getters和Setters
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getWechatId() { return wechatId; }
    public void setWechatId(String wechatId) { this.wechatId = wechatId; }

    public String getReferrerWechatId() { return referrerWechatId; }
    public void setReferrerWechatId(String referrerWechatId) { this.referrerWechatId = referrerWechatId; }

    @Override
    public String toString() {
        return "UserRegistrationDto{" +
                "email='" + email + '\'' +
                ", wechatId='" + wechatId + '\'' +
                ", referrerWechatId='" + referrerWechatId + '\'' +
                '}';
    }
}
