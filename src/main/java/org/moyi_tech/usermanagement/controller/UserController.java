// 更新后的UserController.java - 移除测试endpoint，专注业务功能

package org.moyi_tech.usermanagement.controller;

import org.moyi_tech.usermanagement.dto.UserInfoDto;
import org.moyi_tech.usermanagement.dto.UserNameUpdateDto;
import org.moyi_tech.usermanagement.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@Validated
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getUserProfile(Principal principal) {
        try {
            String email = principal.getName();
            Optional<UserInfoDto> userOpt = userService.findUserByEmail(email);
            
            if (userOpt.isPresent()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("profile", userOpt.get());
                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "User not found");
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PutMapping("/username")
    public ResponseEntity<?> updateUsername(
            @Valid @RequestBody UserNameUpdateDto updateDto,
            Principal principal) {
        try {
            String email = principal.getName();
            UserInfoDto updatedUser = userService.updateUsername(email, updateDto.getUsername());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Username updated successfully");
            response.put("profile", updatedUser);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PutMapping("/wechat")
    public ResponseEntity<?> updateWechatInfo(
            @RequestBody Map<String, String> wechatInfo,
            Principal principal) {
        try {
            String email = principal.getName();
            String wechatId = wechatInfo.get("wechatId");
            String referrerWechatId = wechatInfo.get("referrerWechatId");
            
            UserInfoDto updatedUser = userService.updateWechatInfo(email, wechatId, referrerWechatId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "WeChat information updated successfully");
            response.put("profile", updatedUser);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateUserProfile(
            @RequestBody Map<String, String> profileData,
            Principal principal) {
        try {
            String email = principal.getName();
            UserInfoDto updatedUser = null;
            
            String username = profileData.get("username");
            if (username != null && !username.trim().isEmpty()) {
                updatedUser = userService.updateUsername(email, username);
            }
            
            String wechatId = profileData.get("wechatId");
            String referrerWechatId = profileData.get("referrerWechatId");
            if (wechatId != null || referrerWechatId != null) {
                updatedUser = userService.updateWechatInfo(email, wechatId, referrerWechatId);
            }
            
            if (updatedUser == null) {
                updatedUser = userService.findUserByEmail(email)
                        .orElseThrow(() -> new RuntimeException("User not found"));
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Profile updated successfully");
            response.put("profile", updatedUser);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}