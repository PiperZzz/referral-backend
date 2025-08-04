package org.moyi_tech.usermanagement.controller;

import org.moyi_tech.usermanagement.dto.UserRoleUpdateDto;
import org.moyi_tech.usermanagement.dto.UserInfoDto;
import org.moyi_tech.usermanagement.service.MasterService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/master")
@Validated
public class MasterController {

    private final MasterService masterService;

    public MasterController(MasterService masterService) {
        this.masterService = masterService;
    }

    @GetMapping("/users")
    public ResponseEntity<?> getAllUsersWithRoles() {
        try {
            List<UserInfoDto> users = masterService.getAllUsersWithRoles();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("users", users);
            response.put("total", users.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());

            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/promote-admin")
    public ResponseEntity<?> promoteToAdmin(@Valid @RequestBody UserRoleUpdateDto updateDto) {
        try {
            UserInfoDto user = masterService.promoteToAdmin(updateDto);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "User has been successfully promoted to admin");
            response.put("user", user);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());

            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/demote-admin")
    public ResponseEntity<?> demoteFromAdmin(@RequestBody Map<String, String> request) {
        try {
            String adminEmail = request.get("email");
            if (adminEmail == null || adminEmail.trim().isEmpty()) {
                throw new RuntimeException("Email is required for demotion");
            }

            UserInfoDto user = masterService.demoteFromAdmin(adminEmail);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Admin has been successfully demoted to user");
            response.put("user", user);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());

            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/demotable-admins")
    public ResponseEntity<?> getDemotableAdmins() {
        try {
            List<UserInfoDto> admins = masterService.getDemotableAdmins();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("admins", admins);
            response.put("total", admins.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());

            return ResponseEntity.badRequest().body(response);
        }
    }
}
