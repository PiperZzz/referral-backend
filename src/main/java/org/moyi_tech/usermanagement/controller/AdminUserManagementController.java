package org.moyi_tech.usermanagement.controller;

import org.moyi_tech.usermanagement.dto.UserResponseDto;
import org.moyi_tech.usermanagement.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/users")
@Validated
public class AdminUserManagementController {

    @Autowired
    private UserService userService;

    /**
     * Get all users with role information
     * For Feature 2.2.1 - Admin Portal user management table
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER')")
    public ResponseEntity<?> getAllUsers() {
        try {
            List<UserResponseDto> users = userService.getAllUsers();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("users", users);
            response.put("total", users.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to retrieve users: " + e.getMessage());

            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Activate user
     * For Feature 2.2.2 - Activate/Deactivate button functionality
     */
    @PutMapping("/{userId}/activate")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER')")
    public ResponseEntity<?> activateUser(@PathVariable Long userId) {
        try {
            UserResponseDto user = userService.activateUser(userId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "User activated successfully");
            response.put("user", user);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to activate user: " + e.getMessage());

            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Deactivate user
     * For Feature 2.2.2 - Activate/Deactivate button functionality
     */
    @PutMapping("/{userId}/deactivate")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER')")
    public ResponseEntity<?> deactivateUser(@PathVariable Long userId) {
        try {
            UserResponseDto user = userService.deactivateUser(userId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "User deactivated successfully");
            response.put("user", user);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to deactivate user: " + e.getMessage());

            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Get user candidates (for "Show Refers" functionality)
     * For Feature 2.2.3 - Show Refers button
     */
    @GetMapping("/{userId}/candidates")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER')")
    public ResponseEntity<?> getUserCandidates(@PathVariable Long userId) {
        try {
            List<?> candidates = userService.getUserCandidatesByUserId(userId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("candidates", candidates);
            response.put("total", candidates.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to retrieve user candidates: " + e.getMessage());

            return ResponseEntity.badRequest().body(response);
        }
    }
}