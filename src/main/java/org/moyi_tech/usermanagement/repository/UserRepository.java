package org.moyi_tech.usermanagement.repository;

import org.moyi_tech.usermanagement.constant.UserStatus;
import org.moyi_tech.usermanagement.entity.RoleName;
import org.moyi_tech.usermanagement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    // Find user by email
    Optional<User> findByEmail(String email);
    
    // Check if email exists
    boolean existsByEmail(String email);
    
    // Find users by status
    List<User> findByStatus(UserStatus status);
    
    // Count users by status
    long countByStatus(UserStatus status);
    
    // Find all regular users (non-admin)
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = 'ROLE_USER'")
    List<User> findAllRegularUsers();
    
    // Find user by WeChat ID
    Optional<User> findByWechatId(String wechatId);
    
    // Find users by referrer WeChat ID
    List<User> findByReferrerWechatId(String referrerWechatId);
    
    // Count users by status (breakdown)
    @Query("SELECT u.status, COUNT(u) FROM User u GROUP BY u.status")
    List<Object[]> countUsersByStatus();
    
    // Find users by email (fuzzy search)
    @Query("SELECT u FROM User u WHERE u.email LIKE %:email%")
    List<User> findByEmailContaining(@Param("email") String email);

    // Find users by role
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName")
    List<User> findByRoleName(@Param("roleName") RoleName roleName);
    
    // Count users by role
    @Query("SELECT COUNT(u) FROM User u JOIN u.roles r WHERE r.name = :roleName")
    long countByRoleName(@Param("roleName") RoleName roleName);
    
    // Find all admins
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = 'ROLE_ADMIN'")
    List<User> findAllAdmins();
    
    // Find Master user
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = 'ROLE_MASTER'")
    Optional<User> findMaster();
}