package org.moyi_tech.usermanagement.controller;

import org.moyi_tech.usermanagement.dto.AdminInfoDto;
import org.moyi_tech.usermanagement.dto.AdminInfoResponseDto;
import org.moyi_tech.usermanagement.service.HelpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/manage-admins")
@Validated
public class AdminManagementController {

    @Autowired
    private HelpService helpService;

    /**
     * 获取所有管理员（包含非活跃的）
     */
    @GetMapping
    public ResponseEntity<?> getAllAdmins() {
        try {
            List<AdminInfoResponseDto> admins = helpService.getAllAdmins();

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
     * 添加新管理员
     */
    @PostMapping
    public ResponseEntity<?> addAdmin(@Valid @RequestBody AdminInfoDto adminDto) {
        try {
            AdminInfoResponseDto admin = helpService.addAdmin(adminDto);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "管理员添加成功");
            response.put("admin", admin);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());

            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 更新管理员信息
     */
    @PutMapping("/{adminId}")
    public ResponseEntity<?> updateAdmin(
            @PathVariable Long adminId,
            @Valid @RequestBody AdminInfoDto adminDto) {
        try {
            AdminInfoResponseDto admin = helpService.updateAdmin(adminId, adminDto);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "管理员信息更新成功");
            response.put("admin", admin);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());

            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 切换管理员状态（激活/停用）
     */
    @PutMapping("/{adminId}/toggle-status")
    public ResponseEntity<?> toggleAdminStatus(@PathVariable Long adminId) {
        try {
            AdminInfoResponseDto admin = helpService.toggleAdminStatus(adminId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "管理员状态更新成功");
            response.put("admin", admin);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());

            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 删除管理员
     */
    @DeleteMapping("/{adminId}")
    public ResponseEntity<?> deleteAdmin(@PathVariable Long adminId) {
        try {
            helpService.deleteAdmin(adminId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "管理员删除成功");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());

            return ResponseEntity.badRequest().body(response);
        }
    }
}