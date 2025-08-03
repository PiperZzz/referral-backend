package org.moyi_tech.usermanagement.service;

import org.moyi_tech.usermanagement.dto.RegistrationRequest;
import org.moyi_tech.usermanagement.dto.UserResponseDto;
import org.moyi_tech.usermanagement.entity.Role;
import org.moyi_tech.usermanagement.entity.RoleName;
import org.moyi_tech.usermanagement.entity.User;
import org.moyi_tech.usermanagement.entity.UserStatus;
import org.moyi_tech.usermanagement.repository.RoleRepository;
import org.moyi_tech.usermanagement.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 用户注册
     */
    public UserResponseDto registerUser(RegistrationRequest registrationRequest) {
        // 检查邮箱是否已存在
        if (userRepository.existsByEmail(registrationRequest.getEmail())) {
            throw new RuntimeException("邮箱已被注册: " + registrationRequest.getEmail());
        }

        // 创建新用户
        User user = new User();
        user.setEmail(registrationRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registrationRequest.getPassword()));
        user.setWechatId(registrationRequest.getWechatId());
        user.setReferrerWechatId(registrationRequest.getReferrerWechatId());
        user.setStatus(UserStatus.INACTIVE); // 默认未激活状态

        // 分配默认角色
        Set<Role> roles = new HashSet<>();
        Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
                .orElseThrow(() -> new RuntimeException("默认用户角色不存在"));
        roles.add(userRole);
        user.setRoles(roles);

        // 保存用户
        User savedUser = userRepository.save(user);

        return convertToResponseDto(savedUser);
    }

    /**
     * 根据邮箱查找用户
     */
    public Optional<UserResponseDto> findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(this::convertToResponseDto);
    }

    /**
     * 获取所有普通用户（管理员功能）
     */
    public List<UserResponseDto> getAllRegularUsers() {
        return userRepository.findAllRegularUsers()
                .stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * 暂停用户（管理员功能）
     */
    public UserResponseDto suspendUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在: " + userId));
        
        user.setStatus(UserStatus.INACTIVE);
        User savedUser = userRepository.save(user);
        
        return convertToResponseDto(savedUser);
    }

    /**
     * 检查邮箱是否存在
     */
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    } 

    /**
     * 获取用户的主要角色（用于登录响应）
     */
    public String getUserPrimaryRole(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("用户不存在: " + email));

        // 按优先级确定主要角色：Master > Admin > User
        if (user.getRoles().stream().anyMatch(role -> role.getName() == RoleName.ROLE_MASTER)) {
            return "MASTER";
        } else if (user.getRoles().stream().anyMatch(role -> role.getName() == RoleName.ROLE_ADMIN)) {
            return "ADMIN";
        } else {
            return "USER";
        }
    }

    /**
     * 检查用户是否具有指定角色
     */
    public boolean userHasRole(String email, String roleName) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("用户不存在: " + email));
        
        RoleName targetRole;
        try {
            targetRole = RoleName.valueOf("ROLE_" + roleName.toUpperCase());
        } catch (IllegalArgumentException e) {
            return false;
        }
        
        return user.getRoles().stream()
                .anyMatch(role -> role.getName() == targetRole);
    }

    /**
     * Get all users (for admin management)
     */
    public List<UserResponseDto> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * Activate user (Admin function)
     */
    public UserResponseDto activateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        
        user.setStatus(UserStatus.ACTIVE);
        User savedUser = userRepository.save(user);
        
        return convertToResponseDto(savedUser);
    }

    /**
     * Deactivate user (Admin function)
     */
    public UserResponseDto deactivateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        
        user.setStatus(UserStatus.INACTIVE);
        User savedUser = userRepository.save(user);
        
        return convertToResponseDto(savedUser);
    }

    /**
     * Get user candidates by user ID (for admin "Show Refers" functionality)
     */
    public List<Object> getUserCandidatesByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        
        // This method should be implemented in CandidateService
        // For now, return empty list - this will be properly implemented when we integrate with CandidateService
        return new ArrayList<>();
    }

    /**
     * Reset password
     */
    public void resetPassword(String email, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    /**
     * Update WeChat information
     */
    public UserResponseDto updateWechatInfo(String email, String wechatId, String referrerWechatId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));

        user.setWechatId(wechatId);
        user.setReferrerWechatId(referrerWechatId);

        User savedUser = userRepository.save(user);
        return convertToResponseDto(savedUser);
    }

    /**
     * Convert User entity to UserResponseDto
     */
    private UserResponseDto convertToResponseDto(User user) {
        UserResponseDto dto = new UserResponseDto();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setWechatId(user.getWechatId());
        dto.setReferrerWechatId(user.getReferrerWechatId());
        dto.setStatus(user.getStatus());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        
        // Convert role names to strings
        Set<String> roleNames = user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toSet());
        dto.setRoles(roleNames);
        
        return dto;
    }
}
