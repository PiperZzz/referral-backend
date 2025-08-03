package org.moyi_tech.usermanagement.dto;

import org.moyi_tech.usermanagement.entity.CandidateStatus;
import java.time.LocalDateTime;

public class CandidateInfoDto {
    private Long id;
    private String candidateName;
    private String candidateWechat;
    private CandidateStatus status;
    private String referredByEmail;
    private Long referredByUserId;
    private String adminComments;
    private boolean hasResume;
    private String resumeFilename;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public CandidateInfoDto() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCandidateName() { return candidateName; }
    public void setCandidateName(String candidateName) { this.candidateName = candidateName; }

    public String getCandidateWechat() { return candidateWechat; }
    public void setCandidateWechat(String candidateWechat) { this.candidateWechat = candidateWechat; }

    public CandidateStatus getStatus() { return status; }
    public void setStatus(CandidateStatus status) { this.status = status; }

    public String getReferredByEmail() { return referredByEmail; }
    public void setReferredByEmail(String referredByEmail) { this.referredByEmail = referredByEmail; }

    public Long getReferredByUserId() { return referredByUserId; }
    public void setReferredByUserId(Long referredByUserId) { this.referredByUserId = referredByUserId; }

    public String getAdminComments() { return adminComments; }
    public void setAdminComments(String adminComments) { this.adminComments = adminComments; }

    public boolean isHasResume() { return hasResume; }
    public void setHasResume(boolean hasResume) { this.hasResume = hasResume; }

    public String getResumeFilename() { return resumeFilename; }
    public void setResumeFilename(String resumeFilename) { this.resumeFilename = resumeFilename; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // 编辑相关字段
    private boolean canEdit;
    private boolean canDelete;
    private String statusDescription;

    public boolean isCanEdit() { return canEdit; }
    public void setCanEdit(boolean canEdit) { this.canEdit = canEdit; }

    public boolean isCanDelete() { return canDelete; }
    public void setCanDelete(boolean canDelete) { this.canDelete = canDelete; }

    public String getStatusDescription() { return statusDescription; }
    public void setStatusDescription(String statusDescription) { this.statusDescription = statusDescription; }
}
