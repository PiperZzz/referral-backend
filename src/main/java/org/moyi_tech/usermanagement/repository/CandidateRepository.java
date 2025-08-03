package org.moyi_tech.usermanagement.repository;

import org.moyi_tech.usermanagement.constant.CandidateStatus;
import org.moyi_tech.usermanagement.entity.Candidate;
import org.moyi_tech.usermanagement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CandidateRepository extends JpaRepository<Candidate, Long> {
    
    // Find candidate by WeChat
    Optional<Candidate> findByCandidateWechat(String candidateWechat);
    
    // Check if candidate WeChat exists
    boolean existsByCandidateWechat(String candidateWechat);
    
    // Find candidates by referrer
    List<Candidate> findByReferredBy(User referredBy);
    
    // Find candidates by referrer ID
    List<Candidate> findByReferredById(Long userId);
    
    // Find candidates by status
    List<Candidate> findByStatus(CandidateStatus status);
    
    // Find candidates by referrer and status
    List<Candidate> findByReferredByAndStatus(User referredBy, CandidateStatus status);
    
    // Count candidates by referrer ID
    @Query("SELECT COUNT(c) FROM Candidate c WHERE c.referredBy.id = :userId")
    long countByReferredById(@Param("userId") Long userId);
    
    // Count candidates by referrer and status
    long countByReferredByAndStatus(User referredBy, CandidateStatus status);
    
    // Count candidates by status
    @Query("SELECT c.status, COUNT(c) FROM Candidate c GROUP BY c.status")
    List<Object[]> countCandidatesByStatus();
    
    // Find candidates by name (fuzzy search)
    @Query("SELECT c FROM Candidate c WHERE c.candidateName LIKE %:name%")
    List<Candidate> findByCandidateNameContaining(@Param("name") String name);
    
    // Get latest candidates (for admin view)
    List<Candidate> findTop10ByOrderByCreatedAtDesc();

    // Count candidates by referrer and status list (for open candidates calculation)
    long countByReferredByAndStatusIn(User referredBy, List<CandidateStatus> statuses);
}