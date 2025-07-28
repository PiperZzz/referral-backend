package org.moyi_tech.usermanagement.dto;

import jakarta.validation.constraints.Size;

public class UserProfileUpdateDto {
    @Size(max = 50, message = "微信号长度不能超过50字符")
    private String wechatId;

    public UserProfileUpdateDto() {}

    // Getters and Setters
    public String getWechatId() { return wechatId; }
    public void setWechatId(String wechatId) { this.wechatId = wechatId; }

    @Override
    public String toString() {
        return "UserProfileUpdateDto{" +
                "wechatId='" + wechatId + '\'' +
                '}';
    }
}
