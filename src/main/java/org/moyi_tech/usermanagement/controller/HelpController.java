package org.moyi_tech.usermanagement.controller;

import org.moyi_tech.usermanagement.dto.AdminInfoResponseDto;
import org.moyi_tech.usermanagement.dto.HelpInfoDto;
import org.moyi_tech.usermanagement.service.HelpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/help")
public class HelpController {

    @Autowired
    private HelpService helpService;

    /**
     * 获取完整帮助信息（包含管理员列表）
     */
    @GetMapping("/info")
    public ResponseEntity<?> getHelpInfo(Principal principal) {
        try {
            HelpInfoDto helpInfo = helpService.getHelpInfo();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("helpInfo", helpInfo);
            response.put("message", "获取帮助信息成功");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取帮助信息失败: " + e.getMessage());

            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取管理员列表
     */
    @GetMapping("/admins")
    public ResponseEntity<?> getAdminList() {
        try {
            List<AdminInfoResponseDto> adminList = helpService.getAllActiveAdmins();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("admins", adminList);
            response.put("totalAdmins", adminList.size());
            response.put("message", "获取管理员列表成功");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取管理员列表失败: " + e.getMessage());

            return ResponseEntity.badRequest().body(response);
        }
    }
}