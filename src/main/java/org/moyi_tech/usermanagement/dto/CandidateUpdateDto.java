package org.moyi_tech.usermanagement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CandidateUpdateDto {
    
    @NotBlank(message = "Candidate name cannot be empty")
    @Size(min = 2, max = 50, message = "Candidate name must be between 2 and 50 characters")
    private String candidateName;

    @NotBlank(message = "Candidate WeChat cannot be empty")
    @Size(min = 3, max = 30, message = "WeChat ID must be between 3 and 30 characters")
    private String candidateWechat;

    public CandidateUpdateDto() {}

    public CandidateUpdateDto(String candidateName, String candidateWechat) {
        this.candidateName = candidateName;
        this.candidateWechat = candidateWechat;
    }

    // Getters and Setters
    public String getCandidateName() { return candidateName; }
    public void setCandidateName(String candidateName) { this.candidateName = candidateName; }

    public String getCandidateWechat() { return candidateWechat; }
    public void setCandidateWechat(String candidateWechat) { this.candidateWechat = candidateWechat; }

    @Override
    public String toString() {
        return "CandidateUpdateDto{" +
                "candidateName='" + candidateName + '\'' +
                ", candidateWechat='" + candidateWechat + '\'' +
                '}';
    }
}