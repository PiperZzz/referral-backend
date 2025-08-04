package org.moyi_tech.usermanagement.controller;

import org.moyi_tech.usermanagement.dto.CandidateInfoDto;
import org.moyi_tech.usermanagement.service.AdminService;
import org.moyi_tech.usermanagement.service.CandidateService;
import org.moyi_tech.usermanagement.service.TokenBlacklistService;
import org.moyi_tech.usermanagement.service.UserProfileService;
import org.moyi_tech.usermanagement.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/debug")
public class DebugController {

    @Autowired
    private UserService userService;

    @Autowired
    private CandidateService candidateService;

    @Autowired
    private UserProfileService userProfileService;

    @Autowired
    private AdminService adminService;

    @Autowired
    private TokenBlacklistService tokenBlacklistService;

    // ============== BASIC CONNECTIVITY TESTS ==============

    /**
     * Test basic connectivity and CORS
     */
    @GetMapping("/test")
    public ResponseEntity<?> test() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Backend is running and CORS is configured correctly");
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Test POST request
     */
    @PostMapping("/test-post")
    public ResponseEntity<?> testPost(@RequestBody Map<String, Object> data) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "POST request received successfully");
        response.put("received", data);
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Test OPTIONS request (CORS preflight)
     */
    @RequestMapping(value = "/test-options", method = RequestMethod.OPTIONS)
    public ResponseEntity<?> testOptions() {
        return ResponseEntity.ok().build();
    }

    // ============== AUTHENTICATION ENDPOINTS TEST ==============

    /**
     * Test all authentication endpoints with sample data
     * Note: These are just documentation endpoints - actual testing requires real data
     */
    @GetMapping("/auth/endpoints")
    public ResponseEntity<?> listAuthEndpoints() {
        Map<String, Object> response = new HashMap<>();
        List<Map<String, String>> endpoints = new ArrayList<>();

        // Auth endpoints
        endpoints.add(createEndpoint("POST", "/api/auth/register", "User registration", 
            "{ \"email\": \"test@example.com\", \"password\": \"password123\", \"wechatId\": \"testWechat\" }"));
        endpoints.add(createEndpoint("POST", "/api/auth/login", "User login", 
            "{ \"email\": \"test@example.com\", \"password\": \"password123\" }"));
        endpoints.add(createEndpoint("POST", "/api/auth/logout", "User logout", "{}"));
        endpoints.add(createEndpoint("GET", "/api/auth/status", "Check auth status", ""));
        endpoints.add(createEndpoint("POST", "/api/auth/forget-password", "Forget password", 
            "{ \"email\": \"test@example.com\" }"));
        endpoints.add(createEndpoint("POST", "/api/auth/reset-password", "Reset password", 
            "{ \"token\": \"reset_token\", \"newPassword\": \"newpassword123\" }"));
        endpoints.add(createEndpoint("POST", "/api/auth/refresh-token", "Refresh token", ""));

        response.put("success", true);
        response.put("category", "Authentication Endpoints");
        response.put("endpoints", endpoints);
        response.put("total", endpoints.size());
        
        return ResponseEntity.ok(response);
    }

    // ============== USER ENDPOINTS TEST ==============

    /**
     * Test user-related endpoints
     */
    @GetMapping("/user/endpoints")
    public ResponseEntity<?> listUserEndpoints() {
        Map<String, Object> response = new HashMap<>();
        List<Map<String, String>> endpoints = new ArrayList<>();

        // User endpoints
        endpoints.add(createEndpoint("GET", "/api/users/profile", "Get user profile", ""));
        endpoints.add(createEndpoint("PUT", "/api/users/wechat", "Update WeChat info", 
            "{ \"wechatId\": \"newWechat\", \"referrerWechatId\": \"referrer123\" }"));
        endpoints.add(createEndpoint("GET", "/api/users/test", "Test user endpoint", ""));

        // User Profile endpoints
        endpoints.add(createEndpoint("GET", "/api/user/profile", "Get detailed user profile", ""));
        endpoints.add(createEndpoint("PUT", "/api/user/profile", "Update user profile", 
            "{ \"wechatId\": \"newWechat\", \"referrerWechatId\": \"referrer123\" }"));

        response.put("success", true);
        response.put("category", "User Management Endpoints");
        response.put("endpoints", endpoints);
        response.put("total", endpoints.size());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Test user profile functionality (requires authentication)
     */
    @GetMapping("/user/test-profile")
    public ResponseEntity<?> testUserProfile(Principal principal) {
        if (principal == null) {
            return createAuthRequiredResponse();
        }

        try {
            Map<String, Object> response = new HashMap<>();
            Map<String, Object> tests = new HashMap<>();

            // Test getting user info
            try {
                var userOpt = userService.findUserByEmail(principal.getName());
                tests.put("getUserByEmail", userOpt.isPresent() ? "SUCCESS" : "NO_USER_FOUND");
            } catch (Exception e) {
                tests.put("getUserByEmail", "ERROR: " + e.getMessage());
            }

            // Test getting user profile
            try {
                var profile = userProfileService.getUserProfile(principal.getName());
                tests.put("getUserProfile", "SUCCESS");
            } catch (Exception e) {
                tests.put("getUserProfile", "ERROR: " + e.getMessage());
            }

            response.put("success", true);
            response.put("message", "User profile tests completed");
            response.put("user", principal.getName());
            response.put("tests", tests);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "User profile test failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // ============== CANDIDATE ENDPOINTS TEST ==============

    /**
     * Test candidate-related endpoints
     */
    @GetMapping("/candidate/endpoints")
    public ResponseEntity<?> listCandidateEndpoints() {
        Map<String, Object> response = new HashMap<>();
        List<Map<String, String>> endpoints = new ArrayList<>();

        // Candidate endpoints (User)
        endpoints.add(createEndpoint("POST", "/api/candidates", "Create candidate (multipart)", 
            "candidateName=John&candidateWechat=john123&resumeFile=<file>"));
        endpoints.add(createEndpoint("GET", "/api/candidates/my-candidates", "Get user's candidates", ""));
        endpoints.add(createEndpoint("GET", "/api/candidates/my-stats", "Get candidate statistics", ""));
        endpoints.add(createEndpoint("GET", "/api/candidates/{candidateId}", "Get candidate by ID", ""));
        endpoints.add(createEndpoint("PUT", "/api/candidates/{candidateId}", "Update candidate", 
            "{ \"candidateName\": \"John Updated\", \"candidateWechat\": \"john_updated\" }"));
        endpoints.add(createEndpoint("DELETE", "/api/candidates/{candidateId}", "Delete candidate", ""));
        endpoints.add(createEndpoint("GET", "/api/candidates/{candidateId}/editable", "Check edit permission", ""));
        endpoints.add(createEndpoint("GET", "/api/candidates/{candidateId}/resume", "Download resume", ""));
        endpoints.add(createEndpoint("GET", "/api/candidates/{candidateId}/resume-info", "Get resume info", ""));
        endpoints.add(createEndpoint("GET", "/api/candidates/{candidateId}/permissions", "Get permissions", ""));
        endpoints.add(createEndpoint("PUT", "/api/candidates/{candidateId}/resume", "Update resume (multipart)", 
            "resumeFile=<file>"));
        endpoints.add(createEndpoint("GET", "/api/candidates/{candidateId}/edit", "Get candidate for edit", ""));

        response.put("success", true);
        response.put("category", "Candidate Management Endpoints");
        response.put("endpoints", endpoints);
        response.put("total", endpoints.size());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Test candidate functionality (requires authentication)
     */
    @GetMapping("/candidate/test-operations")
    public ResponseEntity<?> testCandidateOperations(Principal principal) {
        if (principal == null) {
            return createAuthRequiredResponse();
        }

        try {
            Map<String, Object> response = new HashMap<>();
            Map<String, Object> tests = new HashMap<>();

            // Test getting user candidates
            try {
                List<CandidateInfoDto> candidates = candidateService.getUserCandidates(principal.getName());
                tests.put("getUserCandidates", "SUCCESS - Found " + candidates.size() + " candidates");
            } catch (Exception e) {
                tests.put("getUserCandidates", "ERROR: " + e.getMessage());
            }

            // Test getting candidate count
            try {
                long count = candidateService.getUserCandidateCount(principal.getName());
                tests.put("getUserCandidateCount", "SUCCESS - Count: " + count);
            } catch (Exception e) {
                tests.put("getUserCandidateCount", "ERROR: " + e.getMessage());
            }

            response.put("success", true);
            response.put("message", "Candidate operations tests completed");
            response.put("user", principal.getName());
            response.put("tests", tests);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Candidate operations test failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // ============== ADMIN ENDPOINTS TEST ==============

    /**
     * Test admin-related endpoints
     */
    @GetMapping("/admin/endpoints")
    public ResponseEntity<?> listAdminEndpoints() {
        Map<String, Object> response = new HashMap<>();
        List<Map<String, String>> endpoints = new ArrayList<>();

        // Admin Portal endpoints
        endpoints.add(createEndpoint("GET", "/api/admin/users", "Get all users (Admin)", ""));
        endpoints.add(createEndpoint("GET", "/api/admin/management", "Get referral management data (Admin)", ""));
        endpoints.add(createEndpoint("GET", "/api/admin/stats", "Get admin portal stats (Admin)", ""));

        // Admin Candidate endpoints
        endpoints.add(createEndpoint("GET", "/api/admin/candidates", "Get all candidates (Admin)", ""));
        endpoints.add(createEndpoint("GET", "/api/admin/candidates/status/{status}", "Get candidates by status (Admin)", ""));
        endpoints.add(createEndpoint("PUT", "/api/admin/candidates/{candidateId}/status", "Update candidate status (Admin)", 
            "{ \"status\": \"APPROVED\", \"adminComments\": \"Approved by admin\" }"));
        endpoints.add(createEndpoint("GET", "/api/admin/candidates/screening", "Get screening candidates (Admin)", ""));
        endpoints.add(createEndpoint("PUT", "/api/admin/candidates/batch-approve", "Batch approve candidates (Admin)", 
            "[1, 2, 3, 4]"));

        response.put("success", true);
        response.put("category", "Admin Endpoints (Requires ADMIN/MASTER role)");
        response.put("endpoints", endpoints);
        response.put("total", endpoints.size());
        response.put("note", "These endpoints require ADMIN or MASTER role");
        
        return ResponseEntity.ok(response);
    }

    // ============== MASTER ENDPOINTS TEST ==============

    /**
     * Test master-related endpoints
     */
    @GetMapping("/master/endpoints")
    public ResponseEntity<?> listMasterEndpoints() {
        Map<String, Object> response = new HashMap<>();
        List<Map<String, String>> endpoints = new ArrayList<>();

        // Master endpoints
        endpoints.add(createEndpoint("GET", "/api/master/users", "Get all users with roles (Master)", ""));
        endpoints.add(createEndpoint("POST", "/api/master/promote-admin", "Promote user to admin (Master)", 
            "{ \"email\": \"user@example.com\", \"adminComments\": \"Promoted by master\" }"));
        endpoints.add(createEndpoint("POST", "/api/master/demote-admin", "Demote admin to user (Master)", 
            "{ \"email\": \"admin@example.com\" }"));

        response.put("success", true);
        response.put("category", "Master Endpoints (Requires MASTER role)");
        response.put("endpoints", endpoints);
        response.put("total", endpoints.size());
        response.put("note", "These endpoints require MASTER role");
        
        return ResponseEntity.ok(response);
    }

    // ============== UTILITY ENDPOINTS TEST ==============

    /**
     * Test utility endpoints
     */
    @GetMapping("/utility/endpoints")
    public ResponseEntity<?> listUtilityEndpoints() {
        Map<String, Object> response = new HashMap<>();
        List<Map<String, String>> endpoints = new ArrayList<>();

        // Session endpoints
        endpoints.add(createEndpoint("GET", "/api/session/config", "Get session configuration", ""));
        endpoints.add(createEndpoint("POST", "/api/session/heartbeat", "Session heartbeat", ""));

        // Help endpoints
        endpoints.add(createEndpoint("GET", "/api/help/admins", "Get admin WeChat info for help", ""));

        response.put("success", true);
        response.put("category", "Utility Endpoints");
        response.put("endpoints", endpoints);
        response.put("total", endpoints.size());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Test session and utility functionality
     */
    @GetMapping("/utility/test-operations")
    public ResponseEntity<?> testUtilityOperations() {
        try {
            Map<String, Object> response = new HashMap<>();
            Map<String, Object> tests = new HashMap<>();

            // Test token blacklist size
            try {
                int blacklistSize = tokenBlacklistService.getBlacklistSize();
                tests.put("tokenBlacklistSize", "SUCCESS - Size: " + blacklistSize);
            } catch (Exception e) {
                tests.put("tokenBlacklistSize", "ERROR: " + e.getMessage());
            }

            // Test admin service
            try {
                var adminInfo = adminService.getAdminWeChatInfo();
                tests.put("getAdminWeChatInfo", "SUCCESS - Found " + adminInfo.size() + " admins");
            } catch (Exception e) {
                tests.put("getAdminWeChatInfo", "ERROR: " + e.getMessage());
            }

            response.put("success", true);
            response.put("message", "Utility operations tests completed");
            response.put("tests", tests);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Utility operations test failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // ============== COMPREHENSIVE ENDPOINT LISTING ==============

    /**
     * Get all available endpoints in the system
     */
    @GetMapping("/all-endpoints")
    public ResponseEntity<?> listAllEndpoints() {
        Map<String, Object> response = new HashMap<>();
        Map<String, List<Map<String, String>>> categorized = new HashMap<>();

        // Authentication endpoints
        List<Map<String, String>> authEndpoints = new ArrayList<>();
        authEndpoints.add(createEndpoint("POST", "/api/auth/register", "User registration", ""));
        authEndpoints.add(createEndpoint("POST", "/api/auth/login", "User login", ""));
        authEndpoints.add(createEndpoint("POST", "/api/auth/logout", "User logout", ""));
        authEndpoints.add(createEndpoint("GET", "/api/auth/status", "Check auth status", ""));
        categorized.put("Authentication", authEndpoints);

        // User endpoints
        List<Map<String, String>> userEndpoints = new ArrayList<>();
        userEndpoints.add(createEndpoint("GET", "/api/users/profile", "Get user profile", ""));
        userEndpoints.add(createEndpoint("GET", "/api/user/profile", "Get detailed user profile", ""));
        userEndpoints.add(createEndpoint("PUT", "/api/user/profile", "Update user profile", ""));
        categorized.put("User Management", userEndpoints);

        // Candidate endpoints
        List<Map<String, String>> candidateEndpoints = new ArrayList<>();
        candidateEndpoints.add(createEndpoint("POST", "/api/candidates", "Create candidate", ""));
        candidateEndpoints.add(createEndpoint("GET", "/api/candidates/my-candidates", "Get user's candidates", ""));
        candidateEndpoints.add(createEndpoint("PUT", "/api/candidates/{id}", "Update candidate", ""));
        candidateEndpoints.add(createEndpoint("DELETE", "/api/candidates/{id}", "Delete candidate", ""));
        categorized.put("Candidate Management", candidateEndpoints);

        // Admin endpoints
        List<Map<String, String>> adminEndpoints = new ArrayList<>();
        adminEndpoints.add(createEndpoint("GET", "/api/admin/users", "Get all users", "ADMIN"));
        adminEndpoints.add(createEndpoint("GET", "/api/admin/candidates", "Get all candidates", "ADMIN"));
        adminEndpoints.add(createEndpoint("PUT", "/api/admin/candidates/{id}/status", "Update candidate status", "ADMIN"));
        categorized.put("Admin Management", adminEndpoints);

        // Debug endpoints
        List<Map<String, String>> debugEndpoints = new ArrayList<>();
        debugEndpoints.add(createEndpoint("GET", "/api/debug/test", "Basic connectivity test", ""));
        debugEndpoints.add(createEndpoint("POST", "/api/debug/test-post", "POST request test", ""));
        debugEndpoints.add(createEndpoint("GET", "/api/debug/all-endpoints", "List all endpoints", ""));
        categorized.put("Debug & Testing", debugEndpoints);

        response.put("success", true);
        response.put("message", "Complete endpoint listing");
        response.put("categories", categorized);
        response.put("timestamp", System.currentTimeMillis());
        response.put("serverInfo", Map.of(
            "port", "8080",
            "baseUrl", "http://localhost:8080/api",
            "environment", "development"
        ));
        
        return ResponseEntity.ok(response);
    }

    // ============== SYSTEM STATUS CHECK ==============

    /**
     * Comprehensive system health check
     */
    @GetMapping("/system-status")
    public ResponseEntity<?> getSystemStatus() {
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> status = new HashMap<>();

        // Database connectivity
        try {
            userService.getAllUsers();
            status.put("database", "CONNECTED");
        } catch (Exception e) {
            status.put("database", "ERROR: " + e.getMessage());
        }

        // Services availability
        status.put("userService", userService != null ? "AVAILABLE" : "UNAVAILABLE");
        status.put("candidateService", candidateService != null ? "AVAILABLE" : "UNAVAILABLE");
        status.put("adminService", adminService != null ? "AVAILABLE" : "UNAVAILABLE");
        status.put("tokenBlacklistService", tokenBlacklistService != null ? "AVAILABLE" : "UNAVAILABLE");

        // Memory info
        Runtime runtime = Runtime.getRuntime();
        Map<String, String> memoryInfo = new HashMap<>();
        memoryInfo.put("totalMemory", formatBytes(runtime.totalMemory()));
        memoryInfo.put("freeMemory", formatBytes(runtime.freeMemory()));
        memoryInfo.put("maxMemory", formatBytes(runtime.maxMemory()));
        memoryInfo.put("usedMemory", formatBytes(runtime.totalMemory() - runtime.freeMemory()));

        response.put("success", true);
        response.put("message", "System status check completed");
        response.put("status", status);
        response.put("memory", memoryInfo);
        response.put("timestamp", System.currentTimeMillis());
        response.put("uptime", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }

    // ============== HELPER METHODS ==============

    private Map<String, String> createEndpoint(String method, String path, String description, String sampleData) {
        Map<String, String> endpoint = new HashMap<>();
        endpoint.put("method", method);
        endpoint.put("path", path);
        endpoint.put("description", description);
        endpoint.put("sampleData", sampleData);
        return endpoint;
    }

    private ResponseEntity<?> createAuthRequiredResponse() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", "Authentication required for this test");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }
}