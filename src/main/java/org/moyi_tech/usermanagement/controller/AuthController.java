package org.moyi_tech.usermanagement.controller;

import org.moyi_tech.usermanagement.dto.UserLoginDto;
import org.moyi_tech.usermanagement.dto.UserRegistrationDto;
import org.moyi_tech.usermanagement.dto.UserResponseDto;
import org.moyi_tech.usermanagement.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Validated
public class AuthController {

    @Autowired
    private UserService userService;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserRegistrationDto registrationDto) {
        try {
            UserResponseDto user = userService.registerUser(registrationDto);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "用户注册成功，请等待管理员激活");
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
     * 用户登录状态检查
     */
    @GetMapping("/status")
    public ResponseEntity<?> getAuthStatus() {
        Map<String, Object> response = new HashMap<>();
        response.put("authenticated", true);
        response.put("message", "用户已认证");
        
        return ResponseEntity.ok(response);
    }
}
