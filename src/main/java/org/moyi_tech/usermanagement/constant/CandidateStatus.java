package org.moyi_tech.usermanagement.constant;

public enum CandidateStatus {
    SCREENING("Screening"),
    APPROVED("Approved"),
    REJECTED("Rejected"),
    TRAINING("Training"), 
    MARKETING("Marketing"),
    OFFERED("Offered");
    
    private final String description;

    CandidateStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}