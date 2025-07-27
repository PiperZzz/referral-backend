package org.moyi_tech.usermanagement.controller;

import org.moyi_tech.usermanagement.service.TokenBlacklistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/session")
public class SessionController {

    @Autowired
    private TokenBlacklistService tokenBlacklistService;

    /**
     * 获取会话配置信息
     */
    @GetMapping("/config")
    public ResponseEntity<?> getSessionConfig() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("sessionTimeoutSeconds", 600); // 10分钟
        response.put("warningTimeSeconds", 60);     // 剩余1分钟时警告
        response.put("blacklistSize", tokenBlacklistService.getBlacklistSize());
        
        return ResponseEntity.ok(response);
    }

    /**
     * 用户活动心跳（保持会话活跃）
     */
    @PostMapping("/heartbeat")
    public ResponseEntity<?> heartbeat() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "会话活跃");
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }
}
