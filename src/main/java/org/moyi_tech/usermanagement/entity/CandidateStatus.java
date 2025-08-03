package org.moyi_tech.usermanagement.entity;

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