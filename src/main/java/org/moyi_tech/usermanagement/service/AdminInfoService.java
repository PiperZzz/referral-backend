package org.moyi_tech.usermanagement.service;

import org.moyi_tech.usermanagement.dto.AdminInfoUpdateDto;
import org.moyi_tech.usermanagement.dto.AdminInfoDto;
import org.moyi_tech.usermanagement.entity.AdminInfo;
import org.moyi_tech.usermanagement.entity.RoleName;
import org.moyi_tech.usermanagement.entity.User;
import org.moyi_tech.usermanagement.entity.UserStatus;
import org.moyi_tech.usermanagement.repository.AdminInfoRepository;
import org.moyi_tech.usermanagement.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class AdminInfoService {

    @Autowired
    private AdminInfoRepository adminInfoRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Get admin WeChat information for Help overlay (Feature 1.6.2)
     * This method prioritizes AdminInfo entities, but falls back to User entities with ADMIN role
     */
    public List<Map<String, String>> getAdminWeChatInfo() {
        List<Map<String, String>> adminInfo = new ArrayList<>();
        
        // First, try to get from AdminInfo entities
        List<AdminInfo> adminInfoList = adminInfoRepository.findAllActiveAdminsOrdered();
        
        if (!adminInfoList.isEmpty()) {
            for (AdminInfo admin : adminInfoList) {
                if (admin.getWechatId() != null && !admin.getWechatId().trim().isEmpty()) {
                    Map<String, String> info = new HashMap<>();
                    info.put("name", admin.getAdminName());
                    info.put("wechatId", admin.getWechatId());
                    info.put("email", admin.getEmail());
                    info.put("department", admin.getDepartment());
                    info.put("position", admin.getPosition());
                    adminInfo.add(info);
                }
            }
        }
        
        // If no AdminInfo found, fall back to User entities with ADMIN role
        if (adminInfo.isEmpty()) {
            List<User> adminUsers = userRepository.findByRoleName(RoleName.ROLE_ADMIN)
                    .stream()
                    .filter(user -> user.getStatus() == UserStatus.ACTIVE)
                    .filter(user -> user.getWechatId() != null && !user.getWechatId().trim().isEmpty())
                    .collect(Collectors.toList());

            for (User admin : adminUsers) {
                Map<String, String> info = new HashMap<>();
                info.put("name", admin.getEmail().split("@")[0]); // Use email prefix as name
                info.put("wechatId", admin.getWechatId());
                info.put("email", admin.getEmail());
                info.put("department", "System");
                info.put("position", "Administrator");
                adminInfo.add(info);
            }
        }

        // If still no admins found, add default admin info
        if (adminInfo.isEmpty()) {
            Map<String, String> defaultAdmin = new HashMap<>();
            defaultAdmin.put("name", "System Admin");
            defaultAdmin.put("wechatId", "admin-support-2025");
            defaultAdmin.put("email", "admin@moyi-tech.org");
            defaultAdmin.put("department", "Support");
            defaultAdmin.put("position", "System Administrator");
            adminInfo.add(defaultAdmin);
        }

        return adminInfo;
    }

    /**
     * Get complete help information (Feature 1.6)
     */
    public Map<String, Object> getHelpInfo() {
        Map<String, Object> helpInfo = new HashMap<>();
        
        // Get admin information
        List<Map<String, String>> adminInfo = getAdminWeChatInfo();
        
        // Build help content
        helpInfo.put("title", "Need Help?");
        helpInfo.put("message", "Contact our administrators via WeChat for assistance:");
        helpInfo.put("admins", adminInfo);
        helpInfo.put("businessHours", "Available 24/7");
        helpInfo.put("supportEmail", "support@moyi-tech.org");
        
        return helpInfo;
    }

    // ===== AdminInfo Management Methods (for AdminManagementController) =====

    /**
     * Get all active admin info
     */
    public List<AdminInfoDto> getAllActiveAdmins() {
        return adminInfoRepository.findAllActiveAdminsOrdered()
                .stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * Get all admin info (including inactive)
     */
    public List<AdminInfoDto> getAllAdmins() {
        return adminInfoRepository.findAllAdminsOrdered()
                .stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * Add new admin info
     */
    public AdminInfoDto addAdmin(AdminInfoUpdateDto adminDto) {
        // Check if WeChat ID already exists
        if (adminInfoRepository.existsByWechatId(adminDto.getWechatId())) {
            throw new RuntimeException("WeChat ID already exists: " + adminDto.getWechatId());
        }

        // Check if email already exists (if provided)
        if (adminDto.getEmail() != null && 
            !adminDto.getEmail().trim().isEmpty() &&
            adminInfoRepository.existsByEmail(adminDto.getEmail())) {
            throw new RuntimeException("Email already exists: " + adminDto.getEmail());
        }

        AdminInfo adminInfo = convertToEntity(adminDto);
        AdminInfo savedAdmin = adminInfoRepository.save(adminInfo);
        return convertToResponseDto(savedAdmin);
    }

    /**
     * Update admin info
     */
    public AdminInfoDto updateAdmin(Long adminId, AdminInfoUpdateDto adminDto) {
        AdminInfo adminInfo = adminInfoRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin not found: " + adminId));

        // Check WeChat ID uniqueness (if changed)
        if (!adminInfo.getWechatId().equals(adminDto.getWechatId()) &&
            adminInfoRepository.existsByWechatId(adminDto.getWechatId())) {
            throw new RuntimeException("WeChat ID already exists: " + adminDto.getWechatId());
        }

        // Check email uniqueness (if changed)
        if (adminDto.getEmail() != null && 
            !adminDto.getEmail().equals(adminInfo.getEmail()) &&
            adminInfoRepository.existsByEmail(adminDto.getEmail())) {
            throw new RuntimeException("Email already exists: " + adminDto.getEmail());
        }

        // Update admin info
        updateEntityFromDto(adminInfo, adminDto);
        AdminInfo savedAdmin = adminInfoRepository.save(adminInfo);
        return convertToResponseDto(savedAdmin);
    }

    /**
     * Delete admin info
     */
    public void deleteAdmin(Long adminId) {
        if (!adminInfoRepository.existsById(adminId)) {
            throw new RuntimeException("Admin not found: " + adminId);
        }
        adminInfoRepository.deleteById(adminId);
    }

    /**
     * Toggle admin status
     */
    public AdminInfoDto toggleAdminStatus(Long adminId) {
        AdminInfo adminInfo = adminInfoRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin not found: " + adminId));

        adminInfo.setIsActive(!adminInfo.getIsActive());
        AdminInfo savedAdmin = adminInfoRepository.save(adminInfo);
        return convertToResponseDto(savedAdmin);
    }

    /**
     * Initialize default admins
     */
    public void initializeDefaultAdmins() {
        if (adminInfoRepository.count() == 0) {
            // Create system admin
            AdminInfo systemAdmin = new AdminInfo();
            systemAdmin.setAdminName("System Administrator");
            systemAdmin.setWechatId("system-admin-2025");
            systemAdmin.setEmail("admin@moyi-tech.org");
            systemAdmin.setPhoneNumber("400-000-0001");
            systemAdmin.setDepartment("Technology");
            systemAdmin.setPosition("System Administrator");
            systemAdmin.setDescription("Responsible for system maintenance and user support");
            systemAdmin.setIsActive(true);
            systemAdmin.setDisplayOrder(1);
            adminInfoRepository.save(systemAdmin);

            // Create customer service admin
            AdminInfo serviceAdmin = new AdminInfo();
            serviceAdmin.setAdminName("Customer Service");
            serviceAdmin.setWechatId("service-helper-2025");
            serviceAdmin.setEmail("service@moyi-tech.org");
            serviceAdmin.setPhoneNumber("400-000-0002");
            serviceAdmin.setDepartment("Customer Service");
            serviceAdmin.setPosition("Customer Service Specialist");
            serviceAdmin.setDescription("Responsible for user inquiries and problem resolution");
            serviceAdmin.setIsActive(true);
            serviceAdmin.setDisplayOrder(2);
            adminInfoRepository.save(serviceAdmin);

            System.out.println("Default AdminInfo entities created");
        } else {
            System.out.println("AdminInfo entities already exist, skipping initialization");
        }
    }

    // ===== Helper Methods =====

    /**
     * Convert AdminInfo entity to response DTO
     */
    private AdminInfoDto convertToResponseDto(AdminInfo adminInfo) {
        AdminInfoDto dto = new AdminInfoDto();
        dto.setId(adminInfo.getId());
        dto.setAdminName(adminInfo.getAdminName());
        dto.setWechatId(adminInfo.getWechatId());
        dto.setEmail(adminInfo.getEmail());
        dto.setPhoneNumber(adminInfo.getPhoneNumber());
        dto.setDepartment(adminInfo.getDepartment());
        dto.setPosition(adminInfo.getPosition());
        dto.setDescription(adminInfo.getDescription());
        dto.setIsActive(adminInfo.getIsActive());
        dto.setDisplayOrder(adminInfo.getDisplayOrder());
        dto.setCreatedAt(adminInfo.getCreatedAt());
        dto.setUpdatedAt(adminInfo.getUpdatedAt());
        return dto;
    }

    /**
     * Convert DTO to AdminInfo entity
     */
    private AdminInfo convertToEntity(AdminInfoUpdateDto dto) {
        AdminInfo adminInfo = new AdminInfo();
        adminInfo.setAdminName(dto.getAdminName());
        adminInfo.setWechatId(dto.getWechatId());
        adminInfo.setEmail(dto.getEmail());
        adminInfo.setPhoneNumber(dto.getPhoneNumber());
        adminInfo.setDepartment(dto.getDepartment());
        adminInfo.setPosition(dto.getPosition());
        adminInfo.setDescription(dto.getDescription());
        adminInfo.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : true);
        adminInfo.setDisplayOrder(dto.getDisplayOrder() != null ? dto.getDisplayOrder() : 999);
        return adminInfo;
    }

    /**
     * Update entity from DTO
     */
    private void updateEntityFromDto(AdminInfo adminInfo, AdminInfoUpdateDto dto) {
        adminInfo.setAdminName(dto.getAdminName());
        adminInfo.setWechatId(dto.getWechatId());
        adminInfo.setEmail(dto.getEmail());
        adminInfo.setPhoneNumber(dto.getPhoneNumber());
        adminInfo.setDepartment(dto.getDepartment());
        adminInfo.setPosition(dto.getPosition());
        adminInfo.setDescription(dto.getDescription());
        if (dto.getDisplayOrder() != null) {
            adminInfo.setDisplayOrder(dto.getDisplayOrder());
        }
    }
}