package org.moyi_tech.usermanagement.dto;

import org.moyi_tech.usermanagement.constant.CandidateStatus;

import jakarta.validation.constraints.NotNull;

public class CandidateStatusUpdateDto {
    @NotNull(message = "状态不能为空")
    private CandidateStatus status;

    private String adminComments;

    public CandidateStatusUpdateDto() {}

    public CandidateStatus getStatus() { return status; }
    public void setStatus(CandidateStatus status) { this.status = status; }

    public String getAdminComments() { return adminComments; }
    public void setAdminComments(String adminComments) { this.adminComments = adminComments; }
}
