package org.moyi_tech.usermanagement.dto;

import jakarta.validation.constraints.NotBlank;

public class CandidateUpdateDto {
    @NotBlank(message = "候选人姓名不能为空")
    private String candidateName;

    @NotBlank(message = "候选人微信不能为空")
    private String candidateWechat;

    public CandidateUpdateDto() {}

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
