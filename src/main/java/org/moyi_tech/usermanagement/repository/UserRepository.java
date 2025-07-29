package org.moyi_tech.usermanagement.repository;

import org.moyi_tech.usermanagement.entity.RoleName;
import org.moyi_tech.usermanagement.entity.User;
import org.moyi_tech.usermanagement.entity.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    // 根据邮箱查找用户
    Optional<User> findByEmail(String email);
    
    // 检查邮箱是否已存在
    boolean existsByEmail(String email);
    
    // 根据状态查找用户
    List<User> findByStatus(UserStatus status);
    
    // 查找所有普通用户（非管理员）
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = 'ROLE_USER'")
    List<User> findAllRegularUsers();
    
    // 根据微信号查找用户
    Optional<User> findByWechatId(String wechatId);
    
    // 根据推荐人微信号查找用户
    List<User> findByReferrerWechatId(String referrerWechatId);
    
    // 统计各状态用户数量
    @Query("SELECT u.status, COUNT(u) FROM User u GROUP BY u.status")
    List<Object[]> countUsersByStatus();
    
    // 根据邮箱模糊搜索
    @Query("SELECT u FROM User u WHERE u.email LIKE %:email%")
    List<User> findByEmailContaining(@Param("email") String email);

    // 根据角色查找用户
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName")
    List<User> findByRoleName(@Param("roleName") RoleName roleName);
    
    // 统计指定角色的用户数量
    @Query("SELECT COUNT(u) FROM User u JOIN u.roles r WHERE r.name = :roleName")
    long countByRoleName(@Param("roleName") RoleName roleName);
    
    // 查找所有管理员
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = 'ROLE_ADMIN'")
    List<User> findAllAdmins();
    
    // 查找Master用户
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = 'ROLE_MASTER'")
    Optional<User> findMaster();
}