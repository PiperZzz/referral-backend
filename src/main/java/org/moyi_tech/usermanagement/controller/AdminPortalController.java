package org.moyi_tech.usermanagement.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.moyi_tech.usermanagement.dto.UserInfoDto;
import org.moyi_tech.usermanagement.service.AdminService;
import org.moyi_tech.usermanagement.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN') or hasRole('MASTER')")
@Validated
public class AdminPortalController {
    
    private final AdminService adminService;
    private final UserService userService;

    public AdminPortalController (UserService userService, AdminService adminService) {
        this.adminService = adminService;
        this.userService = userService;
    }

    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers() {
        try {
            List<UserInfoDto> users = userService.getAllUsers();

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

    @GetMapping("/users/{userId}/details")
    public ResponseEntity<?> getUserDetails(@PathVariable Long userId) {
        try {
            Map<String, Object> userDetails = adminService.getUserDetails(userId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("userDetails", userDetails);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to retrieve user details: " + e.getMessage());

            return ResponseEntity.badRequest().body(response);
        }
    }

    @PutMapping("/users/{userId}/activate")
    public ResponseEntity<?> activateUser(@PathVariable Long userId) {
        try {
            UserInfoDto user = adminService.activateUser(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "User activated successfully");
            response.put("user", user);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PutMapping("/users/{userId}/deactivate")
    public ResponseEntity<?> deactivateUser(@PathVariable Long userId) {
        try {
            UserInfoDto user = adminService.deactivateUser(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "User deactivated successfully");
            response.put("user", user);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/users/{userId}/candidates")
    public ResponseEntity<?> getUserCandidates(@PathVariable Long userId) {
        try {
            List<?> candidates = adminService.getUserCandidatesByUserId(userId);

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

    @GetMapping("/management")
    public ResponseEntity<?> getReferralManagementData() {
        try {
            Map<String, Object> managementData = adminService.getReferralManagementData();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", managementData);
            response.put("message", "Referral management data retrieved successfully");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to retrieve referral management data: " + e.getMessage());

            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getAdminPortalStats() {
        try {
            Map<String, Object> stats = adminService.getAdminPortalStats();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("stats", stats);
            response.put("message", "Admin portal statistics retrieved successfully");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to retrieve admin portal statistics: " + e.getMessage());

            return ResponseEntity.badRequest().body(response);
        }
    }
}
