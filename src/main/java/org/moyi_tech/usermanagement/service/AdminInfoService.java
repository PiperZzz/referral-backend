package org.moyi_tech.usermanagement.service;

import org.moyi_tech.usermanagement.constant.RoleName;
import org.moyi_tech.usermanagement.constant.UserStatus;
import org.moyi_tech.usermanagement.entity.AdminInfo;
import org.moyi_tech.usermanagement.entity.User;
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
            defaultAdmin.put("email", "admin@moyi-tech.org");
            defaultAdmin.put("department", "Support");
            defaultAdmin.put("position", "System Administrator");
            adminInfo.add(defaultAdmin);
        }

        return adminInfo;
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
}