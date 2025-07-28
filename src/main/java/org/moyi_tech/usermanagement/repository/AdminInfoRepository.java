package org.moyi_tech.usermanagement.repository;

import org.moyi_tech.usermanagement.entity.AdminInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AdminInfoRepository extends JpaRepository<AdminInfo, Long> {
    
    // 根据微信号查找管理员
    Optional<AdminInfo> findByWechatId(String wechatId);
    
    // 检查微信号是否已存在
    boolean existsByWechatId(String wechatId);
    
    // 检查邮箱是否已存在
    boolean existsByEmail(String email);
    
    // 获取所有活跃的管理员，按显示顺序排序
    @Query("SELECT a FROM AdminInfo a WHERE a.isActive = true ORDER BY a.displayOrder ASC, a.adminName ASC")
    List<AdminInfo> findAllActiveAdminsOrdered();
    
    // 获取所有管理员，按显示顺序排序
    @Query("SELECT a FROM AdminInfo a ORDER BY a.displayOrder ASC, a.adminName ASC")
    List<AdminInfo> findAllAdminsOrdered();
    
    // 根据部门查找管理员
    List<AdminInfo> findByDepartmentAndIsActive(String department, Boolean isActive);
    
    // 统计活跃管理员数量
    long countByIsActive(Boolean isActive);
}