package org.moyi_tech.usermanagement.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class AdminInfoDto {
    @NotBlank(message = "管理员姓名不能为空")
    private String adminName;

    @NotBlank(message = "微信号不能为空")
    private String wechatId;

    @Email(message = "请输入有效的邮箱地址")
    private String email;

    private String phoneNumber;
    private String department;
    private String position;
    private String description;
    private Boolean isActive = true;
    private Integer displayOrder = 0;

    public AdminInfoDto() {}

    // Getters and Setters
    public String getAdminName() { return adminName; }
    public void setAdminName(String adminName) { this.adminName = adminName; }

    public String getWechatId() { return wechatId; }
    public void setWechatId(String wechatId) { this.wechatId = wechatId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public Integer getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }
}