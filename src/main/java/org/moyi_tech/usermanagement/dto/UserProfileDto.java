package org.moyi_tech.usermanagement.dto;

public class UserProfileDto {
    private Long id;
    private String name;           // 用户名称 = email（只读）
    private String email;          // 邮箱（只读）
    private String wechatId;       // 微信号（可修改）
    private Integer userLevel;     // 用户等级（只读）
    private Long openCandidates;   // 开放候选人数量（只读）

    public UserProfileDto() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getWechatId() { return wechatId; }
    public void setWechatId(String wechatId) { this.wechatId = wechatId; }

    public Integer getUserLevel() { return userLevel; }
    public void setUserLevel(Integer userLevel) { this.userLevel = userLevel; }

    public Long getOpenCandidates() { return openCandidates; }
    public void setOpenCandidates(Long openCandidates) { this.openCandidates = openCandidates; }

    @Override
    public String toString() {
        return "UserProfileDto{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", wechatId='" + wechatId + '\'' +
                ", userLevel=" + userLevel +
                ", openCandidates=" + openCandidates +
                '}';
    }
}
