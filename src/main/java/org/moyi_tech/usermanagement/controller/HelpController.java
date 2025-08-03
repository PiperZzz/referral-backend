package org.moyi_tech.usermanagement.controller;

import org.moyi_tech.usermanagement.service.AdminInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/help")
public class HelpController {

    @Autowired
    private AdminInfoService adminInfoService;

    /**
     * Get admin WeChat information for Help overlay
     * For Feature 1.6 - Help Overlay
     */
    @GetMapping("/admins")
    public ResponseEntity<?> getAdminWeChatInfo() {
        try {
            List<Map<String, String>> adminInfo = adminInfoService.getAdminWeChatInfo();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("admins", adminInfo);
            response.put("totalAdmins", adminInfo.size());
            response.put("message", "Admin information retrieved successfully");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to retrieve admin information: " + e.getMessage());

            return ResponseEntity.badRequest().body(response);
        }
    }
}