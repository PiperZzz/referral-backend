package org.moyi_tech.usermanagement.controller;

import org.moyi_tech.usermanagement.service.AdminService;
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
    private AdminService adminService;

    @GetMapping("/admins")
    public ResponseEntity<?> getAdminWeChatInfo() {
        try {
            List<Map<String, String>> adminInfo = adminService.getAdminWeChatInfo();

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