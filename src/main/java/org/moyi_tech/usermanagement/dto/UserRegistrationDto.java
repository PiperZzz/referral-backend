package org.moyi_tech.usermanagement.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UserRegistrationDto {
    
    @Email(message = "请输入有效的邮箱地址")
    @NotBlank(message = "邮箱不能为空")
    private String email;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在6-20字符之间")
    private String password;

    private String wechatId;
    
    private String referrerWechatId;

    // 构造函数
    public UserRegistrationDto() {}

    public UserRegistrationDto(String email, String password) {
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
