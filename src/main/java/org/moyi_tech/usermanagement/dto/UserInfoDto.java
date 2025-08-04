package org.moyi_tech.usermanagement.dto;

import java.time.LocalDateTime;
import java.util.Set;

import org.moyi_tech.usermanagement.constant.UserStatus;

public class UserInfoDto {
    
    private Long id;
    private String email;
    private String wechatId;
    private String username;
    private String referrerWechatId;
    private UserStatus status;
    private Set<String> roles;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public UserInfoDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getWechatId() { return wechatId; }
    public void setWechatId(String wechatId) { this.wechatId = wechatId; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getReferrerWechatId() { return referrerWechatId; }
    public void setReferrerWechatId(String referrerWechatId) { this.referrerWechatId = referrerWechatId; }

    public UserStatus getStatus() { return status; }
    public void setStatus(UserStatus status) { this.status = status; }

    public Set<String> getRoles() { return roles; }
    public void setRoles(Set<String> roles) { this.roles = roles; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}