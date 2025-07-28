package org.moyi_tech.usermanagement.controller;

import org.moyi_tech.usermanagement.dto.UserProfileDto;
import org.moyi_tech.usermanagement.dto.UserProfileUpdateDto;
import org.moyi_tech.usermanagement.service.UserProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user/profile")
@Validated
public class UserProfileController {

    @Autowired
    private UserProfileService userProfileService;

    /**
     * 获取用户个人资料
     */
    @GetMapping
    public ResponseEntity<?> getUserProfile(Principal principal) {
        try {
            UserProfileDto profile = userProfileService.getUserProfile(principal.getName());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("profile", profile);
            response.put("message", "获取用户资料成功");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取用户资料失败: " + e.getMessage());

            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 更新用户个人资料
     */
    @PutMapping
    public ResponseEntity<?> updateUserProfile(
            @Valid @RequestBody UserProfileUpdateDto updateDto,
            Principal principal) {
        try {
            UserProfileDto profile = userProfileService.updateUserProfile(principal.getName(), updateDto);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("profile", profile);
            response.put("message", "用户资料更新成功");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "用户资料更新失败: " + e.getMessage());

            return ResponseEntity.badRequest().body(response);
        }
    }
}
