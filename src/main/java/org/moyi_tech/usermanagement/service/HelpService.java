package org.moyi_tech.usermanagement.service;

import org.moyi_tech.usermanagement.dto.AdminInfoDto;
import org.moyi_tech.usermanagement.dto.AdminInfoResponseDto;
import org.moyi_tech.usermanagement.dto.HelpInfoDto;
import org.moyi_tech.usermanagement.entity.AdminInfo;
import org.moyi_tech.usermanagement.repository.AdminInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class HelpService {

    @Autowired
    private AdminInfoRepository adminInfoRepository;

    /**
     * 获取完整的帮助信息（包含管理员列表）
     */
    public HelpInfoDto getHelpInfo() {
        HelpInfoDto helpInfo = new HelpInfoDto();
        
        // 获取活跃的管理员列表
        List<AdminInfoResponseDto> adminList = getAllActiveAdmins();
        
        helpInfo.setSupportMessage("遇到问题？别担心！以下是我们的管理员团队，请选择合适的管理员联系：");
        helpInfo.setBusinessHours("7x24小时在线服务");
        helpInfo.setAdminList(adminList);

        return helpInfo;
    }

    /**
     * 获取所有活跃的管理员列表
     */
    public List<AdminInfoResponseDto> getAllActiveAdmins() {
        return adminInfoRepository.findAllActiveAdminsOrdered()
                .stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * 获取所有管理员列表（管理员功能）
     */
    public List<AdminInfoResponseDto> getAllAdmins() {
        return adminInfoRepository.findAllAdminsOrdered()
                .stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * 添加新管理员
     */
    public AdminInfoResponseDto addAdmin(AdminInfoDto adminDto) {
        // 检查微信号是否已存在
        if (adminInfoRepository.existsByWechatId(adminDto.getWechatId())) {
            throw new RuntimeException("微信号已存在: " + adminDto.getWechatId());
        }

        // 检查邮箱是否已存在（如果提供了邮箱）
        if (adminDto.getEmail() != null && 
            !adminDto.getEmail().trim().isEmpty() &&
            adminInfoRepository.existsByEmail(adminDto.getEmail())) {
            throw new RuntimeException("邮箱已存在: " + adminDto.getEmail());
        }

        AdminInfo adminInfo = convertToEntity(adminDto);
        AdminInfo savedAdmin = adminInfoRepository.save(adminInfo);
        return convertToResponseDto(savedAdmin);
    }

    /**
     * 更新管理员信息
     */
    public AdminInfoResponseDto updateAdmin(Long adminId, AdminInfoDto adminDto) {
        AdminInfo adminInfo = adminInfoRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("管理员不存在: " + adminId));

        // 检查微信号唯一性（如果有变更）
        if (!adminInfo.getWechatId().equals(adminDto.getWechatId()) &&
            adminInfoRepository.existsByWechatId(adminDto.getWechatId())) {
            throw new RuntimeException("微信号已存在: " + adminDto.getWechatId());
        }

        // 检查邮箱唯一性（如果有变更）
        if (adminDto.getEmail() != null && 
            !adminDto.getEmail().equals(adminInfo.getEmail()) &&
            adminInfoRepository.existsByEmail(adminDto.getEmail())) {
            throw new RuntimeException("邮箱已存在: " + adminDto.getEmail());
        }

        // 更新管理员信息
        updateEntityFromDto(adminInfo, adminDto);
        AdminInfo savedAdmin = adminInfoRepository.save(adminInfo);
        return convertToResponseDto(savedAdmin);
    }

    /**
     * 删除管理员
     */
    public void deleteAdmin(Long adminId) {
        if (!adminInfoRepository.existsById(adminId)) {
            throw new RuntimeException("管理员不存在: " + adminId);
        }
        adminInfoRepository.deleteById(adminId);
    }

    /**
     * 切换管理员状态
     */
    public AdminInfoResponseDto toggleAdminStatus(Long adminId) {
        AdminInfo adminInfo = adminInfoRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("管理员不存在: " + adminId));

        adminInfo.setIsActive(!adminInfo.getIsActive());
        AdminInfo savedAdmin = adminInfoRepository.save(adminInfo);
        return convertToResponseDto(savedAdmin);
    }

    /**
     * 初始化默认管理员
     */
    public void initializeDefaultAdmins() {
        if (adminInfoRepository.count() == 0) {
            // 创建系统管理员
            AdminInfo systemAdmin = new AdminInfo();
            systemAdmin.setAdminName("系统管理员");
            systemAdmin.setWechatId("system_admin_2025");
            systemAdmin.setEmail("admin@company.com");
            systemAdmin.setPhoneNumber("400-000-0001");
            systemAdmin.setDepartment("技术部");
            systemAdmin.setPosition("系统管理员");
            systemAdmin.setDescription("负责系统维护和用户支持");
            systemAdmin.setIsActive(true);
            systemAdmin.setDisplayOrder(1);
            adminInfoRepository.save(systemAdmin);

            // 创建客服管理员
            AdminInfo serviceAdmin = new AdminInfo();
            serviceAdmin.setAdminName("客服小助手");
            serviceAdmin.setWechatId("service_helper_2025");
            serviceAdmin.setEmail("service@company.com");
            serviceAdmin.setPhoneNumber("400-000-0002");
            serviceAdmin.setDepartment("客服部");
            serviceAdmin.setPosition("客服专员");
            serviceAdmin.setDescription("负责用户咨询和问题解答");
            serviceAdmin.setIsActive(true);
            serviceAdmin.setDisplayOrder(2);
            adminInfoRepository.save(serviceAdmin);

            // 创建业务管理员
            AdminInfo businessAdmin = new AdminInfo();
            businessAdmin.setAdminName("业务经理");
            businessAdmin.setWechatId("business_manager_2025");
            businessAdmin.setEmail("business@company.com");
            businessAdmin.setPhoneNumber("400-000-0003");
            businessAdmin.setDepartment("业务部");
            businessAdmin.setPosition("业务经理");
            businessAdmin.setDescription("负责业务推广和合作洽谈");
            businessAdmin.setIsActive(true);
            businessAdmin.setDisplayOrder(3);
            adminInfoRepository.save(businessAdmin);

            System.out.println("Created default admins");
        }
    }

    /**
     * 转换 DTO 为实体
     */
    private AdminInfo convertToEntity(AdminInfoDto dto) {
        AdminInfo entity = new AdminInfo();
        updateEntityFromDto(entity, dto);
        return entity;
    }

    /**
     * 用 DTO 更新实体
     */
    private void updateEntityFromDto(AdminInfo entity, AdminInfoDto dto) {
        entity.setAdminName(dto.getAdminName());
        entity.setWechatId(dto.getWechatId());
        entity.setEmail(dto.getEmail());
        entity.setPhoneNumber(dto.getPhoneNumber());
        entity.setDepartment(dto.getDepartment());
        entity.setPosition(dto.getPosition());
        entity.setDescription(dto.getDescription());
        entity.setIsActive(dto.getIsActive());
        entity.setDisplayOrder(dto.getDisplayOrder());
    }

    /**
     * 转换为响应DTO
     */
    private AdminInfoResponseDto convertToResponseDto(AdminInfo entity) {
        AdminInfoResponseDto dto = new AdminInfoResponseDto();
        dto.setId(entity.getId());
        dto.setAdminName(entity.getAdminName());
        dto.setWechatId(entity.getWechatId());
        dto.setEmail(entity.getEmail());
        dto.setPhoneNumber(entity.getPhoneNumber());
        dto.setDepartment(entity.getDepartment());
        dto.setPosition(entity.getPosition());
        dto.setDescription(entity.getDescription());
        dto.setIsActive(entity.getIsActive());
        dto.setDisplayOrder(entity.getDisplayOrder());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }
}