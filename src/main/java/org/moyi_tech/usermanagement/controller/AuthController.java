package org.moyi_tech.usermanagement.controller;

import org.moyi_tech.usermanagement.dto.*;
import org.moyi_tech.usermanagement.service.EmailService;
import org.moyi_tech.usermanagement.service.TokenBlacklistService;
import org.moyi_tech.usermanagement.service.UserService;
import org.moyi_tech.usermanagement.util.JwtUtils;
import org.moyi_tech.usermanagement.util.RoleUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Validated
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private EmailService emailService;

    @Autowired
    private TokenBlacklistService tokenBlacklistService;
    
    /**
     * 用户注册
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegistrationRequest registrationRequest) {
        try {
            UserInfoDto user = userService.registerUser(registrationRequest);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "User registered successfully");
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
     * 用户登录
     */
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getEmail(),
                    loginRequest.getPassword())
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtils.generateJwtToken(authentication);

            // 获取用户信息
            UserInfoDto userInfo = userService.findUserByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found with email: " + loginRequest.getEmail()));

            // 确定主要角色和默认路由
            String primaryRole = RoleUtils.determinePrimaryRole(userInfo.getRoles());
            String defaultRoute = RoleUtils.getDefaultRouteForRole(primaryRole);

            // 更新 LoginResponse 构造
            LoginResponse loginResponse = new LoginResponse(jwt, userInfo, primaryRole, defaultRoute);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Login successful");
            response.put("data", loginResponse);

            return ResponseEntity.ok(response);

        } catch (AuthenticationException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Email or password is incorrect");
            
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/forget-password")
    public ResponseEntity<?> forgetPassword(@Valid @RequestBody PasswordResetRequest request) {
        try {
            if (!userService.existsByEmail(request.getEmail())) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Email not found");
                return ResponseEntity.badRequest().body(response);
            }

            String resetToken = jwtUtils.generatePasswordResetToken(request.getEmail());
            emailService.sendPasswordResetEmail(request.getEmail(), resetToken);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Password reset email sent successfully");
            response.put("token", resetToken);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            String resetToken = jwtUtils.generatePasswordResetToken(request.getEmail());
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to send to " + e.getMessage());
             response.put("token", resetToken);
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 重置密码
     */
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody PasswordResetRequest request) {
        try {
            // 验证token
            if (!jwtUtils.validateJwtToken(request.getToken())) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Password reset token is invalid or expired");
                return ResponseEntity.badRequest().body(response);
            }

            String email = jwtUtils.getUserNameFromJwtToken(request.getToken());
            userService.resetPassword(email, request.getNewPassword());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Password reset successfully");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 检查登录状态
     */
    @GetMapping("/status")
    public ResponseEntity<?> getAuthStatus(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            String email = authentication.getName();
            UserInfoDto user = userService.findUserByEmail(email)
                .orElse(null);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("authenticated", true);
            response.put("user", user);
            
            return ResponseEntity.ok(response);
        } else {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("authenticated", false);
            response.put("message", "未登录");
            
            return ResponseEntity.ok(response);
        }
    }


    /**
     * 用户登出
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        try {
            // 获取JWT token
            String headerAuth = request.getHeader("Authorization");
            if (headerAuth != null && headerAuth.startsWith("Bearer ")) {
                String jwt = headerAuth.substring(7);
                
                // 将token添加到黑名单
                tokenBlacklistService.blacklistToken(jwt);
                
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Logout successful");
                
                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Failed to find token in request header");
                
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to logout " + e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 刷新token（如果需要延长会话）
     */
    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(HttpServletRequest request, Authentication authentication) {
        try {
            // 获取当前token
            String headerAuth = request.getHeader("Authorization");
            if (headerAuth != null && headerAuth.startsWith("Bearer ")) {
                String oldToken = headerAuth.substring(7);
                
                // 检查token是否即将过期
                if (jwtUtils.isTokenExpiringSoon(oldToken)) {
                    // 生成新token
                    String newToken = jwtUtils.generateJwtToken(authentication);
                    
                    // 将旧token加入黑名单
                    tokenBlacklistService.blacklistToken(oldToken);
                    
                    // 获取用户信息
                    UserInfoDto userInfo = userService.findUserByEmail(authentication.getName())
                            .orElseThrow(() -> new RuntimeException("User not found with email: " + authentication.getName()));

                    LoginResponse loginResponse = new LoginResponse(newToken, userInfo);

                    Map<String, Object> response = new HashMap<>();
                    response.put("success", true);
                    response.put("message", "Token refreshed successfully");
                    response.put("data", loginResponse);

                    return ResponseEntity.ok(response);
                } else {
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", false);
                    response.put("message", "Token is not expiring soon, no need to refresh");
                    
                    return ResponseEntity.badRequest().body(response);
                }
            } else {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Failed to find token in request header");
                
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to refresh token" + e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 检查token状态
     */
    @GetMapping("/token-status")
    public ResponseEntity<?> getTokenStatus(HttpServletRequest request) {
        try {
            String headerAuth = request.getHeader("Authorization");
            if (headerAuth != null && headerAuth.startsWith("Bearer ")) {
                String jwt = headerAuth.substring(7);
                
                boolean isValid = jwtUtils.validateJwtToken(jwt);
                boolean isBlacklisted = tokenBlacklistService.isTokenBlacklisted(jwt);
                boolean isExpiringSoon = jwtUtils.isTokenExpiringSoon(jwt);
                long remainingTime = jwtUtils.getTokenRemainingTime(jwt);
                
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("isValid", isValid && !isBlacklisted);
                response.put("isBlacklisted", isBlacklisted);
                response.put("isExpiringSoon", isExpiringSoon);
                response.put("remainingTimeMs", remainingTime);
                response.put("remainingTimeSeconds", remainingTime / 1000);
                
                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Failed to find token in request header");
                
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }
}