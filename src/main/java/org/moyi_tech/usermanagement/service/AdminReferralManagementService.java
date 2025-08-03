package org.moyi_tech.usermanagement.service;

import org.moyi_tech.usermanagement.constant.CandidateStatus;
import org.moyi_tech.usermanagement.dto.CandidateInfoDto;
import org.moyi_tech.usermanagement.entity.User;
import org.moyi_tech.usermanagement.entity.UserStatus;
import org.moyi_tech.usermanagement.repository.CandidateRepository;
import org.moyi_tech.usermanagement.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdminReferralManagementService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CandidateRepository candidateRepository;

    /**
     * Get referral management data for admin portal
     * For Feature 2.2.1 - Referral Management table
     */
    public Map<String, Object> getReferralManagementData() {
        // Get all active users
        List<User> activeUsers = userRepository.findByStatus(UserStatus.ACTIVE);
        
        List<Map<String, Object>> referralData = new ArrayList<>();
        
        for (User user : activeUsers) {
            Map<String, Object> userReferralInfo = new HashMap<>();
            
            // Basic user info
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

    /**
     * Get admin portal statistics
     * For Feature 2.2.1 - Total Open Referrals and Total Connections
     */
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

    /**
     * Get user details for referral management
     */
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

    /**
     * Count users who have at least one open candidate
     */
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

    /**
     * Convert Candidate to Admin-specific CandidateResponseDto
     * Includes admin-specific permissions
     */
    private CandidateInfoDto convertToAdminCandidateResponseDto(org.moyi_tech.usermanagement.entity.Candidate candidate) {
        CandidateInfoDto dto = new CandidateInfoDto();
        dto.setId(candidate.getId());
        dto.setCandidateName(candidate.getCandidateName());
        dto.setCandidateWechat(candidate.getCandidateWechat());
        dto.setStatus(candidate.getStatus());
        dto.setStatusDescription(candidate.getStatus().getDescription());
        dto.setReferredByEmail(candidate.getReferredBy().getEmail());
        dto.setReferredByUserId(candidate.getReferredBy().getId());
        dto.setAdminComments(candidate.getAdminComments());
        dto.setHasResume(candidate.getResumeFile() != null);
        dto.setResumeFilename(candidate.getResumeFilename());
        dto.setCreatedAt(candidate.getCreatedAt());
        dto.setUpdatedAt(candidate.getUpdatedAt());

        // Admin permissions - can update status and add comments, but cannot edit basic info
        dto.setCanEdit(false); // Admins cannot edit candidate name, wechat, etc.
        dto.setCanDelete(true); // Admins can delete candidates
        
        return dto;
    }
}