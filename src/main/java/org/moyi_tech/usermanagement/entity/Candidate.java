package org.moyi_tech.usermanagement.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "candidates")
@EntityListeners(AuditingEntityListener.class)
public class Candidate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "候选人姓名不能为空")
    @Column(name = "candidate_name", nullable = false)
    private String candidateName;

    @NotBlank(message = "候选人微信不能为空")
    @Column(name = "candidate_wechat", unique = true, nullable = false)
    private String candidateWechat;

    @Lob
    @Column(name = "resume_file")
    private byte[] resumeFile;

    @Column(name = "resume_filename")
    private String resumeFilename;

    @Column(name = "resume_content_type")
    private String resumeContentType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CandidateStatus status = CandidateStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "referred_by_user_id", nullable = false)
    private User referredBy;

    @Column(name = "admin_comments")
    private String adminComments;

    @CreatedDate
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 构造函数
    public Candidate() {}

    public Candidate(String candidateName, String candidateWechat, User referredBy) {
        this.candidateName = candidateName;
        this.candidateWechat = candidateWechat;
        this.referredBy = referredBy;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCandidateName() { return candidateName; }
    public void setCandidateName(String candidateName) { this.candidateName = candidateName; }

    public String getCandidateWechat() { return candidateWechat; }
    public void setCandidateWechat(String candidateWechat) { this.candidateWechat = candidateWechat; }

    public byte[] getResumeFile() { return resumeFile; }
    public void setResumeFile(byte[] resumeFile) { this.resumeFile = resumeFile; }

    public String getResumeFilename() { return resumeFilename; }
    public void setResumeFilename(String resumeFilename) { this.resumeFilename = resumeFilename; }

    public String getResumeContentType() { return resumeContentType; }
    public void setResumeContentType(String resumeContentType) { this.resumeContentType = resumeContentType; }

    public CandidateStatus getStatus() { return status; }
    public void setStatus(CandidateStatus status) { this.status = status; }

    public User getReferredBy() { return referredBy; }
    public void setReferredBy(User referredBy) { this.referredBy = referredBy; }

    public String getAdminComments() { return adminComments; }
    public void setAdminComments(String adminComments) { this.adminComments = adminComments; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return "Candidate{" +
                "id=" + id +
                ", candidateName='" + candidateName + '\'' +
                ", candidateWechat='" + candidateWechat + '\'' +
                ", status=" + status +
                ", referredBy=" + (referredBy != null ? referredBy.getEmail() : null) +
                ", createdAt=" + createdAt +
                '}';
    }
}
