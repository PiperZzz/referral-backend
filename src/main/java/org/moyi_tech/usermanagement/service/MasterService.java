package org.moyi_tech.usermanagement.service;

import org.moyi_tech.usermanagement.dto.UserRoleUpdateDto;
import org.moyi_tech.usermanagement.dto.UserInfoDto;
import org.moyi_tech.usermanagement.entity.Role;
import org.moyi_tech.usermanagement.entity.RoleName;
import org.moyi_tech.usermanagement.entity.User;
import org.moyi_tech.usermanagement.entity.UserStatus;
import org.moyi_tech.usermanagement.repository.RoleRepository;
import org.moyi_tech.usermanagement.repository.UserRepository;
import org.moyi_tech.usermanagement.util.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class MasterService {

    private static final String MASTER_EMAIL = "mark.wang@moyi-tech.org";
    private static final int MAX_ADMIN_COUNT = 2;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @Autowired
    private JwtUtils jwtUtils;

    /**
     * 初始化 Master 账号
     */
    public void initializeMaster() {
        // 检查是否已存在 Master 账号
        if (userRepository.findByEmail(MASTER_EMAIL).isPresent()) {
            System.out.println("Master account already exists");
            return;
        }

        // 创建 Master 账号
        User master = new User();
        master.setEmail(MASTER_EMAIL);
        
        // 生成临时密码
        String tempPassword = UUID.randomUUID().toString().substring(0, 12);
        master.setPassword(passwordEncoder.encode(tempPassword));
        master.setStatus(UserStatus.ACTIVE);

        // 分配所有角色（Master 具有所有权限）
        Set<Role> roles = new HashSet<>();
        roles.add(roleRepository.findByName(RoleName.ROLE_USER)
                .orElseThrow(() -> new RuntimeException("USER角色不存在")));
        roles.add(roleRepository.findByName(RoleName.ROLE_ADMIN)
                .orElseThrow(() -> new RuntimeException("ADMIN角色不存在")));
        roles.add(roleRepository.findByName(RoleName.ROLE_MASTER)
                .orElseThrow(() -> new RuntimeException("MASTER角色不存在")));
        master.setRoles(roles);

        userRepository.save(master);

        // 生成重置密码链接并发送邮件
        String resetToken = jwtUtils.generatePasswordResetToken(MASTER_EMAIL);
        sendMasterWelcomeEmail(resetToken);

        System.out.println("Master account created and welcome email sent");
    }

    /**
     * 获取所有用户及其角色信息
     */
    public List<UserInfoDto> getAllUsersWithRoles() {
        return userRepository.findAll()
                .stream()
                .map(this::convertToUserResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * 提升用户为管理员
     */
    public UserInfoDto promoteToAdmin(UserRoleUpdateDto updateDto) {
        // 检查目标用户是否存在
        User targetUser = userRepository.findByEmail(updateDto.getEmail())
                .orElseThrow(() -> new RuntimeException("用户不存在: " + updateDto.getEmail()));

        // 检查用户是否已经是管理员
        if (hasRole(targetUser, RoleName.ROLE_ADMIN)) {
            throw new RuntimeException("用户已经是管理员");
        }

        // 检查当前管理员数量
        long currentAdminCount = userRepository.countByRoleName(RoleName.ROLE_ADMIN);
        
        if (currentAdminCount >= MAX_ADMIN_COUNT) {
            // 需要降级一个现有管理员
            if (updateDto.getDemoteAdminEmail() == null || updateDto.getDemoteAdminEmail().trim().isEmpty()) {
                throw new RuntimeException("管理员名额已满（最多" + MAX_ADMIN_COUNT + "个），请选择要降级的管理员");
            }
            
            demoteFromAdmin(updateDto.getDemoteAdminEmail());
        }

        // 添加管理员角色
        Role adminRole = roleRepository.findByName(RoleName.ROLE_ADMIN)
                .orElseThrow(() -> new RuntimeException("ADMIN角色不存在"));
        targetUser.getRoles().add(adminRole);
        targetUser.setStatus(UserStatus.ACTIVE); // 确保账号激活

        User savedUser = userRepository.save(targetUser);
        return convertToUserResponseDto(savedUser);
    }

    /**
     * 降级管理员为普通用户
     */
    public UserInfoDto demoteFromAdmin(String adminEmail) {
        User adminUser = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new RuntimeException("用户不存在: " + adminEmail));

        // 检查用户是否是管理员
        if (!hasRole(adminUser, RoleName.ROLE_ADMIN)) {
            throw new RuntimeException("用户不是管理员");
        }

        // 不能降级 Master
        if (hasRole(adminUser, RoleName.ROLE_MASTER)) {
            throw new RuntimeException("不能降级Master账号");
        }

        // 移除管理员角色
        adminUser.getRoles().removeIf(role -> role.getName() == RoleName.ROLE_ADMIN);

        User savedUser = userRepository.save(adminUser);
        return convertToUserResponseDto(savedUser);
    }

    /**
     * 获取可降级的管理员列表
     */
    public List<UserInfoDto> getDemotableAdmins() {
        return userRepository.findAllAdmins()
                .stream()
                .filter(user -> !hasRole(user, RoleName.ROLE_MASTER)) // 排除Master
                .map(this::convertToUserResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * Master 首次登录重置密码
     */
    public void resetMasterPassword(String token, String newPassword) {
        if (!jwtUtils.validateJwtToken(token)) {
            throw new RuntimeException("重置链接已过期或无效");
        }

        String email = jwtUtils.getUserNameFromJwtToken(token);
        if (!MASTER_EMAIL.equals(email)) {
            throw new RuntimeException("无效的重置请求");
        }

        User master = userRepository.findByEmail(MASTER_EMAIL)
                .orElseThrow(() -> new RuntimeException("Master账号不存在"));

        master.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(master);
    }

    /**
     * 发送Master欢迎邮件
     */
    private void sendMasterWelcomeEmail(String resetToken) {
        try {
            String resetUrl = "https://referral.moyi-tech.org/master/reset-password?token=" + resetToken;
            
            String subject = "Welcome to Referral System - Master Account Created";
            String message = "Dear Master,\n\n" +
                    "Your Master account has been created for the Referral System.\n\n" +
                    "Please click the following link to set your password:\n\n" +
                    resetUrl + "\n\n" +
                    "This link will expire in 1 hour.\n\n" +
                    "Best regards,\n" +
                    "Referral System";

            emailService.sendSimpleEmail(MASTER_EMAIL, subject, message);
        } catch (Exception e) {
            System.err.println("Failed to send master welcome email: " + e.getMessage());
        }
    }

    /**
     * 检查用户是否具有指定角色
     */
    private boolean hasRole(User user, RoleName roleName) {
        return user.getRoles().stream()
                .anyMatch(role -> role.getName() == roleName);
    }

    /**
     * 转换为用户响应DTO
     */
    private UserInfoDto convertToUserResponseDto(User user) {
        UserInfoDto dto = new UserInfoDto();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setWechatId(user.getWechatId());
        dto.setReferrerWechatId(user.getReferrerWechatId());
        dto.setStatus(user.getStatus());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        
        // 转换角色名称
        Set<String> roleNames = user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toSet());
        dto.setRoles(roleNames);
        
        return dto;
    }
}