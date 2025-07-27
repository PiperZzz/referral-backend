package org.moyi_tech.usermanagement.entity;

public enum CandidateStatus {
    PENDING("待审核"),
    APPROVED("已审核"),
    REJECTED("已拒绝");

    private final String description;

    CandidateStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
