package org.moyi_tech.usermanagement.controller;

import org.moyi_tech.usermanagement.dto.PasswordResetRequest;
import org.moyi_tech.usermanagement.dto.UserRoleUpdateDto;
import org.moyi_tech.usermanagement.dto.UserInfoDto;
import org.moyi_tech.usermanagement.service.MasterService;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private MasterService masterService;

    /**
     * 获取所有用户及角色信息
     */
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

    /**
     * 提升用户为管理员
     */
    @PostMapping("/promote-admin")
    public ResponseEntity<?> promoteToAdmin(@Valid @RequestBody UserRoleUpdateDto updateDto) {
        try {
            UserInfoDto user = masterService.promoteToAdmin(updateDto);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "用户已成功提升为管理员");
            response.put("user", user);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());

            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 降级管理员为普通用户
     */
    @PostMapping("/demote-admin")
    public ResponseEntity<?> demoteFromAdmin(@RequestBody Map<String, String> request) {
        try {
            String adminEmail = request.get("email");
            if (adminEmail == null || adminEmail.trim().isEmpty()) {
                throw new RuntimeException("邮箱不能为空");
            }

            UserInfoDto user = masterService.demoteFromAdmin(adminEmail);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "管理员已成功降级为普通用户");
            response.put("user", user);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());

            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取可降级的管理员列表
     */
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

    /**
     * Master首次重置密码
     */
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody PasswordResetRequest passwordResetRequest) {
        try {
            masterService.resetMasterPassword(passwordResetRequest.getToken(), passwordResetRequest.getNewPassword());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Master密码重置成功");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());

            return ResponseEntity.badRequest().body(response);
        }
    }
}
