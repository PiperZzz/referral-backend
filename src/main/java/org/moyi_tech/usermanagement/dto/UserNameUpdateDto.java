package org.moyi_tech.usermanagement.dto;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

public class UserNameUpdateDto {
    @Size(min = 2, max = 50, message = "Username must be between 2 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_\\s]+$", message = "Username can only contain letters, numbers, underscores and spaces")
    private String username;

    public UserNameUpdateDto() {}

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    @Override
    public String toString() {
        return "UserNameUpdateDto{" +
                "username='" + username + '\'' +
                '}';
    }
}