package org.moyi_tech.usermanagement.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class UserRoleUpdateDto {
    @Email(message = "Please enter a valid email address")
    @NotBlank(message = "Email cannot be empty")
    private String email;

    @NotNull(message = "Target role cannot be empty")
    private String targetRole;

    private String demoteAdminEmail;

    public UserRoleUpdateDto() {}

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTargetRole() { return targetRole; }
    public void setTargetRole(String targetRole) { this.targetRole = targetRole; }

    public String getDemoteAdminEmail() { return demoteAdminEmail; }
    public void setDemoteAdminEmail(String demoteAdminEmail) { this.demoteAdminEmail = demoteAdminEmail; }
}
