package org.moyi_tech.usermanagement.dto;

import java.util.List;

public class HelpInfoDto {
    private String supportMessage;
    private String businessHours;
    private List<AdminInfoDto> adminList;
    private Integer totalAdmins;

    public HelpInfoDto() {}

    public String getSupportMessage() { return supportMessage; }
    public void setSupportMessage(String supportMessage) { this.supportMessage = supportMessage; }

    public String getBusinessHours() { return businessHours; }
    public void setBusinessHours(String businessHours) { this.businessHours = businessHours; }

    public List<AdminInfoDto> getAdminList() { return adminList; }
    public void setAdminList(List<AdminInfoDto> adminList) { 
        this.adminList = adminList;
        this.totalAdmins = adminList != null ? adminList.size() : 0;
    }

    public Integer getTotalAdmins() { return totalAdmins; }
    public void setTotalAdmins(Integer totalAdmins) { this.totalAdmins = totalAdmins; }
}