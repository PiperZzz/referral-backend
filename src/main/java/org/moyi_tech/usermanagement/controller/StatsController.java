package org.moyi_tech.usermanagement.controller;

import org.moyi_tech.usermanagement.service.StatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/stats")
public class StatsController {

    @Autowired
    private StatsService statsService;

    /**
     * Get admin portal statistics
     * For Feature 2.2.1 - Admin Portal statistics
     * Requires ADMIN or MASTER role
     */
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER')")
    public ResponseEntity<?> getAdminStats() {
        try {
            Map<String, Object> stats = statsService.getAdminStats();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("stats", stats);
            response.put("message", "Admin statistics retrieved successfully");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to retrieve admin statistics: " + e.getMessage());

            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Get general system statistics
     * Accessible by all authenticated users
     */
    @GetMapping("/system")
    public ResponseEntity<?> getSystemStats() {
        try {
            Map<String, Object> stats = statsService.getSystemStats();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("stats", stats);
            response.put("message", "System statistics retrieved successfully");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to retrieve system statistics: " + e.getMessage());

            return ResponseEntity.badRequest().body(response);
        }
    }
}