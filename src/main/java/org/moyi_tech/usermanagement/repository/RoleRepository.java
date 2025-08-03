package org.moyi_tech.usermanagement.repository;

import org.moyi_tech.usermanagement.constant.RoleName;
import org.moyi_tech.usermanagement.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    
    // 根据角色名查找角色
    Optional<Role> findByName(RoleName name);
    
    // 检查角色是否存在
    boolean existsByName(RoleName name);
}