package org.moyi_tech.usermanagement.dto;

import jakarta.validation.constraints.NotBlank;

public class CandidateCreateDto {
    @NotBlank(message = "Candidate name cannot be empty")
    private String candidateName;

    @NotBlank(message = "Candidate WeChat cannot be empty")
    private String candidateWechat;

    public CandidateCreateDto() {}

    public String getCandidateName() { return candidateName; }
    public void setCandidateName(String candidateName) { this.candidateName = candidateName; }

    public String getCandidateWechat() { return candidateWechat; }
    public void setCandidateWechat(String candidateWechat) { this.candidateWechat = candidateWechat; }

    @Override
    public String toString() {
        return "CandidateCreateDto{" +
                "candidateName='" + candidateName + '\'' +
                ", candidateWechat='" + candidateWechat + '\'' +
                '}';
    }
}
