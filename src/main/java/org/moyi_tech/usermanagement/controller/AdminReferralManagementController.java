package org.moyi_tech.usermanagement.controller;

import org.moyi_tech.usermanagement.dto.CandidateResponseDto;
import org.moyi_tech.usermanagement.service.AdminReferralManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/referrals")
@Validated
public class AdminReferralManagementController {

    @Autowired
    private AdminReferralManagementService adminReferralManagementService;

    /**
     * Get referral management table data
     * For Feature 2.2.1 - Admin Portal Referral Management table
     */
    @GetMapping("/management")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER')")
    public ResponseEntity<?> getReferralManagementData() {
        try {
            Map<String, Object> managementData = adminReferralManagementService.getReferralManagementData();

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

    /**
     * Get user candidates (for "Show Refers" functionality)
     * For Feature 2.2.3 - Show Refers button
     */
    @GetMapping("/user/{userId}/candidates")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER')")
    public ResponseEntity<?> getUserCandidates(@PathVariable Long userId) {
        try {
            List<CandidateResponseDto> candidates = adminReferralManagementService.getUserCandidates(userId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("candidates", candidates);
            response.put("total", candidates.size());
            response.put("userId", userId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to retrieve user candidates: " + e.getMessage());

            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Get admin portal statistics
     * For Feature 2.2.1 - Total Open Referrals and Total Connections
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER')")
    public ResponseEntity<?> getAdminPortalStats() {
        try {
            Map<String, Object> stats = adminReferralManagementService.getAdminPortalStats();

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

    /**
     * Get user details for referral management
     * For Feature 2.2.1 - User information in referral table
     */
    @GetMapping("/user/{userId}/details")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER')")
    public ResponseEntity<?> getUserDetails(@PathVariable Long userId) {
        try {
            Map<String, Object> userDetails = adminReferralManagementService.getUserDetails(userId);

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
}