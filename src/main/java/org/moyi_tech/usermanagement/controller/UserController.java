package org.moyi_tech.usermanagement.controller;

import org.moyi_tech.usermanagement.dto.UserResponseDto;
import org.moyi_tech.usermanagement.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 获取当前用户信息
     */
    @GetMapping("/profile")
    public ResponseEntity<?> getUserProfile(Principal principal) {
        try {
            String email = principal.getName();
            Optional<UserResponseDto> userOpt = userService.findUserByEmail(email);
            
            if (userOpt.isPresent()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("user", userOpt.get());
                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "用户不存在");
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 更新微信信息
     */
    @PutMapping("/wechat")
    public ResponseEntity<?> updateWechatInfo(
            @RequestBody Map<String, String> wechatInfo,
            Principal principal) {
        try {
            String email = principal.getName();
            String wechatId = wechatInfo.get("wechatId");
            String referrerWechatId = wechatInfo.get("referrerWechatId");
            
            UserResponseDto updatedUser = userService.updateWechatInfo(email, wechatId, referrerWechatId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "微信信息更新成功");
            response.put("user", updatedUser);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 测试端点
     */
    @GetMapping("/test")
    public ResponseEntity<?> test() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "User API正常工作!");
        response.put("authenticated", auth.isAuthenticated());
        response.put("username", auth.getName());
        response.put("authorities", auth.getAuthorities());
        
        return ResponseEntity.ok(response);
    }
}
