package org.moyi_tech.usermanagement.service;

import org.moyi_tech.usermanagement.constant.CandidateStatus;
import org.moyi_tech.usermanagement.constant.RoleName;
import org.moyi_tech.usermanagement.constant.UserStatus;
import org.moyi_tech.usermanagement.dto.CandidateInfoDto;
import org.moyi_tech.usermanagement.dto.UserInfoDto;
import org.moyi_tech.usermanagement.entity.AdminInfo;
import org.moyi_tech.usermanagement.entity.Candidate;
import org.moyi_tech.usermanagement.entity.User;
import org.moyi_tech.usermanagement.repository.AdminInfoRepository;
import org.moyi_tech.usermanagement.repository.CandidateRepository;
import org.moyi_tech.usermanagement.repository.UserRepository;
import org.moyi_tech.usermanagement.util.DtoUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final CandidateRepository candidateRepository;
    private final AdminInfoRepository adminInfoRepository;

    public AdminService(UserRepository userRepository, CandidateRepository candidateRepository, AdminInfoRepository adminInfoRepository) {
        this.userRepository = userRepository;
        this.candidateRepository = candidateRepository;
        this.adminInfoRepository = adminInfoRepository;
    }

    public Map<String, Object> getReferralManagementData() {
        List<User> activeUsers = userRepository.findByStatus(UserStatus.ACTIVE);
        
        List<Map<String, Object>> referralData = new ArrayList<>();
        
        for (User user : activeUsers) {
            Map<String, Object> userReferralInfo = new HashMap<>();
            
            userReferralInfo.put("userId", user.getId());
            userReferralInfo.put("referralName", user.getEmail().split("@")[0]); // Use email prefix as name
            userReferralInfo.put("email", user.getEmail());
            userReferralInfo.put("wechat", user.getWechatId());
            userReferralInfo.put("status", user.getStatus().getDescription());
            
            // Calculate user level (number of OFFERED candidates)
            long userLevel = candidateRepository.countByReferredByAndStatus(user, CandidateStatus.OFFERED);
            userReferralInfo.put("level", userLevel);
            
            // Calculate open candidates count
            List<CandidateStatus> openStatuses = Arrays.asList(
                CandidateStatus.APPROVED,
                CandidateStatus.TRAINING,
                CandidateStatus.MARKETING
            );
            long openCandidates = candidateRepository.countByReferredByAndStatusIn(user, openStatuses);
            userReferralInfo.put("openCandidates", openCandidates);
            
            // Total candidates count
            long totalCandidates = candidateRepository.countByReferredById(user.getId());
            userReferralInfo.put("totalCandidates", totalCandidates);
            
            referralData.add(userReferralInfo);
        }
        
        // Sort by level descending, then by total candidates descending
        referralData.sort((a, b) -> {
            Long levelA = (Long) a.get("level");
            Long levelB = (Long) b.get("level");
            int levelCompare = levelB.compareTo(levelA);
            if (levelCompare != 0) return levelCompare;
            
            Long totalA = (Long) a.get("totalCandidates");
            Long totalB = (Long) b.get("totalCandidates");
            return totalB.compareTo(totalA);
        });
        
        Map<String, Object> result = new HashMap<>();
        result.put("referrals", referralData);
        result.put("totalUsers", referralData.size());
        
        return result;
    }

    /**
     * Get user candidates for "Show Refers" functionality
     * For Feature 2.2.3 - Show Refers button
     */
    public List<CandidateInfoDto> getUserCandidates(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        return candidateRepository.findByReferredBy(user)
                .stream()
                .map(this::convertToAdminCandidateResponseDto)
                .collect(Collectors.toList());
    }

    public Map<String, Object> getAdminPortalStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // Total Connections: number of active users
        long totalConnections = userRepository.countByStatus(UserStatus.ACTIVE);
        
        // Total Open Referrals: number of users with at least one "Open Candidate"
        long totalOpenReferrals = getUsersWithOpenCandidatesCount();
        
        stats.put("totalConnections", totalConnections);
        stats.put("totalOpenReferrals", totalOpenReferrals);
        
        // Additional useful stats
        stats.put("totalUsers", userRepository.count());
        stats.put("totalCandidates", candidateRepository.count());
        
        return stats;
    }

    public Map<String, Object> getUserDetails(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        Map<String, Object> userDetails = new HashMap<>();
        userDetails.put("id", user.getId());
        userDetails.put("email", user.getEmail());
        userDetails.put("wechatId", user.getWechatId());
        userDetails.put("status", user.getStatus());
        userDetails.put("createdAt", user.getCreatedAt());
        userDetails.put("updatedAt", user.getUpdatedAt());
        
        // Role information
        Set<String> roles = user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toSet());
        userDetails.put("roles", roles);
        
        // Candidate statistics
        long totalCandidates = candidateRepository.countByReferredById(user.getId());
        long userLevel = candidateRepository.countByReferredByAndStatus(user, CandidateStatus.OFFERED);
        
        List<CandidateStatus> openStatuses = Arrays.asList(
            CandidateStatus.APPROVED,
            CandidateStatus.TRAINING,
            CandidateStatus.MARKETING
        );
        long openCandidates = candidateRepository.countByReferredByAndStatusIn(user, openStatuses);
        
        userDetails.put("totalCandidates", totalCandidates);
        userDetails.put("userLevel", userLevel);
        userDetails.put("openCandidates", openCandidates);
        
        return userDetails;
    }

    @Transactional
    public UserInfoDto activateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        
        user.setStatus(UserStatus.ACTIVE);
        User savedUser = userRepository.save(user);
        
        return convertToResponseDto(savedUser);
    }

    @Transactional
    public UserInfoDto deactivateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        
        user.setStatus(UserStatus.INACTIVE);
        User savedUser = userRepository.save(user);
        
        return convertToResponseDto(savedUser);
    }

    /**
     * Get user candidates by user ID (for admin "Show Refers" functionality)
     */
    public List<Object> getUserCandidatesByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        // TODO implmenent Show Refers 
        // This method should be implemented in CandidateService
        // For now, return empty list - this will be properly implemented when we integrate with CandidateService
        return new ArrayList<>();
    }

        public List<Map<String, String>> getAdminWeChatInfo() {
        List<Map<String, String>> adminInfo = new ArrayList<>();
        
        // try to get from AdminInfo entities
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

    private long getUsersWithOpenCandidatesCount() {
        List<CandidateStatus> openStatuses = Arrays.asList(
            CandidateStatus.APPROVED,
            CandidateStatus.TRAINING,
            CandidateStatus.MARKETING
        );
        
        return candidateRepository.findAll()
            .stream()
            .filter(candidate -> openStatuses.contains(candidate.getStatus()))
            .map(candidate -> candidate.getReferredBy().getId())
            .distinct()
            .count();
    }

    private UserInfoDto convertToResponseDto(User user) {
       return DtoUtils.convertToResponseDto(user);
    }

    private CandidateInfoDto convertToAdminCandidateResponseDto(Candidate candidate) {
        return DtoUtils.convertToAdminCandidateResponseDto(candidate);
    }
}