package org.moyi_tech.usermanagement.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class UserRoleUpdateDto {
    @Email(message = "请输入有效的邮箱地址")
    @NotBlank(message = "邮箱不能为空")
    private String email;

    @NotNull(message = "目标角色不能为空")
    private String targetRole; // "ADMIN" 或 "USER"

    private String demoteAdminEmail; // 当提升为 Admin 时，如果需要降级的 Admin 邮箱

    public UserRoleUpdateDto() {}

    // Getters and Setters
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTargetRole() { return targetRole; }
    public void setTargetRole(String targetRole) { this.targetRole = targetRole; }

    public String getDemoteAdminEmail() { return demoteAdminEmail; }
    public void setDemoteAdminEmail(String demoteAdminEmail) { this.demoteAdminEmail = demoteAdminEmail; }
}
