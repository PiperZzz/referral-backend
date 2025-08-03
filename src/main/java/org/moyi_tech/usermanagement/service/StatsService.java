package org.moyi_tech.usermanagement.service;

import org.moyi_tech.usermanagement.constant.CandidateStatus;
import org.moyi_tech.usermanagement.entity.UserStatus;
import org.moyi_tech.usermanagement.repository.CandidateRepository;
import org.moyi_tech.usermanagement.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StatsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CandidateRepository candidateRepository;

    /**
     * Get admin portal statistics
     * For Feature 2.2.1 - Admin Portal statistics
     */
    public Map<String, Object> getAdminStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // Total Connections: number of active users
        long totalConnections = userRepository.countByStatus(UserStatus.ACTIVE);
        
        // Total Open Referrals: number of users with at least one "Open Candidate"
        // Open Candidates are those with status: Approved, Training, Marketing
        long totalOpenReferrals = getUsersWithOpenCandidatesCount();
        
        stats.put("totalConnections", totalConnections);
        stats.put("totalOpenReferrals", totalOpenReferrals);
        
        return stats;
    }

    /**
     * Get general system statistics
     */
    public Map<String, Object> getSystemStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // Basic counts
        stats.put("totalUsers", userRepository.count());
        stats.put("activeUsers", userRepository.countByStatus(UserStatus.ACTIVE));
        stats.put("inactiveUsers", userRepository.countByStatus(UserStatus.INACTIVE));
        stats.put("totalCandidates", candidateRepository.count());
        
        // Candidate status breakdown
        Map<String, Long> candidatesByStatus = getCandidatesByStatusBreakdown();
        stats.put("candidatesByStatus", candidatesByStatus);
        
        return stats;
    }

    /**
     * Count users who have at least one open candidate
     * Open candidates are those with status: Approved, Training, Marketing
     */
    private long getUsersWithOpenCandidatesCount() {
        List<CandidateStatus> openStatuses = Arrays.asList(
            CandidateStatus.APPROVED,
            CandidateStatus.TRAINING,
            CandidateStatus.MARKETING
        );
        
        // This query counts distinct users who have candidates in open statuses
        return candidateRepository.findAll()
            .stream()
            .filter(candidate -> openStatuses.contains(candidate.getStatus()))
            .map(candidate -> candidate.getReferredBy().getId())
            .distinct()
            .count();
    }

    /**
     * Get breakdown of candidates by status
     */
    private Map<String, Long> getCandidatesByStatusBreakdown() {
        Map<String, Long> breakdown = new HashMap<>();
        
        List<Object[]> results = candidateRepository.countCandidatesByStatus();
        for (Object[] result : results) {
            CandidateStatus status = (CandidateStatus) result[0];
            Long count = (Long) result[1];
            breakdown.put(status.name(), count);
        }
        
        return breakdown;
    }
}