package org.moyi_tech.usermanagement.service;

import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class TokenBlacklistService {

    // 使用内存存储黑名单token（生产环境建议使用Redis）
    private final Set<String> blacklistedTokens = ConcurrentHashMap.newKeySet();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public TokenBlacklistService() {
        // 每小时清理一次过期的黑名单token
        scheduler.scheduleAtFixedRate(this::cleanupExpiredTokens, 1, 1, TimeUnit.HOURS);
    }

    /**
     * 将token添加到黑名单
     */
    public void blacklistToken(String token) {
        blacklistedTokens.add(token);
        
        // 设置token在24小时后自动从黑名单移除（JWT过期时间）
        scheduler.schedule(() -> blacklistedTokens.remove(token), 24, TimeUnit.HOURS);
    }

    /**
     * 检查token是否在黑名单中
     */
    public boolean isTokenBlacklisted(String token) {
        return blacklistedTokens.contains(token);
    }

    /**
     * 清理过期的黑名单token
     */
    private void cleanupExpiredTokens() {
        // 这里可以根据token的过期时间进行清理
        // 简化实现：依赖于blacklistToken方法中的定时清理
        System.out.println("清理过期的黑名单token，当前黑名单大小: " + blacklistedTokens.size());
    }

    /**
     * 获取黑名单大小（用于监控）
     */
    public int getBlacklistSize() {
        return blacklistedTokens.size();
    }
}
