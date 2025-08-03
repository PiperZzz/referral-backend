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
     * Get user profile
     */
    @GetMapping
    public ResponseEntity<?> getUserProfile(Principal principal) {
        try {
            UserProfileDto profile = userProfileService.getUserProfile(principal.getName());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("profile", profile);
            response.put("message", "User profile retrieved successfully");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to retrieve user profile: " + e.getMessage());

            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Update user profile
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
            response.put("message", "User profile updated successfully");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to update user profile: " + e.getMessage());

            return ResponseEntity.badRequest().body(response);
        }
    }
}