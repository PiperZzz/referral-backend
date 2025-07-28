package org.moyi_tech.usermanagement.repository;

import org.moyi_tech.usermanagement.entity.Candidate;
import org.moyi_tech.usermanagement.entity.CandidateStatus;
import org.moyi_tech.usermanagement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CandidateRepository extends JpaRepository<Candidate, Long> {
    
    // 根据候选人微信查找
    Optional<Candidate> findByCandidateWechat(String candidateWechat);
    
    // 检查候选人微信是否已存在
    boolean existsByCandidateWechat(String candidateWechat);
    
    // 根据推荐人查找候选人
    List<Candidate> findByReferredBy(User referredBy);
    
    // 根据推荐人ID查找候选人
    List<Candidate> findByReferredById(Long userId);
    
    // 根据状态查找候选人
    List<Candidate> findByStatus(CandidateStatus status);
    
    // 根据推荐人和状态查找
    List<Candidate> findByReferredByAndStatus(User referredBy, CandidateStatus status);
    
    // 统计推荐人的候选人数量
    @Query("SELECT COUNT(c) FROM Candidate c WHERE c.referredBy.id = :userId")
    long countByReferredById(@Param("userId") Long userId);
    
    // 统计各状态的候选人数量
    @Query("SELECT c.status, COUNT(c) FROM Candidate c GROUP BY c.status")
    List<Object[]> countCandidatesByStatus();
    
    // 根据候选人姓名模糊搜索
    @Query("SELECT c FROM Candidate c WHERE c.candidateName LIKE %:name%")
    List<Candidate> findByCandidateNameContaining(@Param("name") String name);
    
    // 获取最新的候选人（用于管理员查看）
    List<Candidate> findTop10ByOrderByCreatedAtDesc();

    // 统计用户在指定状态列表中的候选人数量
    long countByReferredByAndStatusIn(User referredBy, List<CandidateStatus> statuses);
}
