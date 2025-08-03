package org.moyi_tech.usermanagement.service;

import org.moyi_tech.usermanagement.entity.Role;
import org.moyi_tech.usermanagement.entity.RoleName;
import org.moyi_tech.usermanagement.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

@Service
public class RoleInitializationService implements CommandLineRunner {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private AdminInfoService helpService;

    @Autowired
    private MasterService masterService;

    @Override
    public void run(String... args) throws Exception {
        // 初始化基础角色
        initializeRoles();
        
        // 初始化默认管理员
        initializeDefaultAdmins();
        
        // 初始化Master账号
        initializeMaster();
    }

    private void initializeRoles() {
        // 创建普通用户角色
        if (!roleRepository.existsByName(RoleName.ROLE_USER)) {
            Role userRole = new Role(RoleName.ROLE_USER);
            roleRepository.save(userRole);
            System.out.println("Created ROLE_USER");
        }

        // 创建管理员角色
        if (!roleRepository.existsByName(RoleName.ROLE_ADMIN)) {
            Role adminRole = new Role(RoleName.ROLE_ADMIN);
            roleRepository.save(adminRole);
            System.out.println("Created ROLE_ADMIN");
        }

        // 创建Master角色
        if (!roleRepository.existsByName(RoleName.ROLE_MASTER)) {
            Role masterRole = new Role(RoleName.ROLE_MASTER);
            roleRepository.save(masterRole);
            System.out.println("Created ROLE_MASTER");
        }
    }

    /**
     * 初始化默认管理员
     */
    private void initializeDefaultAdmins() {
        helpService.initializeDefaultAdmins();
        System.out.println("Default admins initialized");
    }

    /**
     * 初始化Master账号
     */
    private void initializeMaster() {
        masterService.initializeMaster();
        System.out.println("Master account initialization completed");
    }
}
