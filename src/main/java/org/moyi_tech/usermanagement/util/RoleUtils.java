package org.moyi_tech.usermanagement.util;

import java.util.Set;

public class RoleUtils {
    
    /**
     * 根据角色集合确定主要角色
     */
    public static String determinePrimaryRole(Set<String> roleNames) {
        if (roleNames.contains("ROLE_MASTER")) {
            return "MASTER";
        } else if (roleNames.contains("ROLE_ADMIN")) {
            return "ADMIN";
        } else {
            return "USER";
        }
    }
    
    /**
     * 根据角色获取默认路由
     */
    public static String getDefaultRouteForRole(String primaryRole) {
        switch (primaryRole) {
            case "MASTER":
                return "/master/dashboard";
            case "ADMIN":
                return "/admin/dashboard";
            case "USER":
            default:
                return "/user/profile";
        }
    }
    
    /**
     * 检查角色集合是否包含Master权限
     */
    public static boolean isMaster(Set<String> roleNames) {
        return roleNames.contains("ROLE_MASTER");
    }
    
    /**
     * 检查角色集合是否包含Admin权限（包括Master）
     */
    public static boolean isAdmin(Set<String> roleNames) {
        return roleNames.contains("ROLE_ADMIN") || roleNames.contains("ROLE_MASTER");
    }
    
    /**
     * 检查角色集合是否包含User权限
     */
    public static boolean isUser(Set<String> roleNames) {
        return roleNames.contains("ROLE_USER");
    }
}
