package org.moyi_tech.usermanagement.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/debug")
public class DebugController {

    /**
     * 测试CORS和基本连接
     */
    @GetMapping("/test")
    public ResponseEntity<?> test() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "后端连接正常");
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }

    /**
     * 测试POST请求
     */
    @PostMapping("/test-post")
    public ResponseEntity<?> testPost(@RequestBody Map<String, Object> data) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "POST请求正常");
        response.put("received", data);
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }

    /**
     * 测试OPTIONS请求
     */
    @RequestMapping(value = "/test-options", method = RequestMethod.OPTIONS)
    public ResponseEntity<?> testOptions() {
        return ResponseEntity.ok().build();
    }
}