package org.moyi_tech.usermanagement.service;

import org.moyi_tech.usermanagement.dto.CandidateCreateDto;
import org.moyi_tech.usermanagement.dto.CandidateResponseDto;
import org.moyi_tech.usermanagement.dto.CandidateStatusUpdateDto;
import org.moyi_tech.usermanagement.entity.Candidate;
import org.moyi_tech.usermanagement.entity.CandidateStatus;
import org.moyi_tech.usermanagement.entity.User;
import org.moyi_tech.usermanagement.repository.CandidateRepository;
import org.moyi_tech.usermanagement.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class CandidateService {

    @Autowired
    private CandidateRepository candidateRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    /**
     * 创建新候选人（带简历上传）
     */
    public CandidateResponseDto createCandidate(CandidateCreateDto createDto, 
                                              MultipartFile resumeFile, 
                                              String userEmail) {
        // 检查候选人微信是否已存在
        if (candidateRepository.existsByCandidateWechat(createDto.getCandidateWechat())) {
            throw new RuntimeException("候选人微信号已存在: " + createDto.getCandidateWechat());
        }

        // 获取推荐用户
        User referredBy = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("用户不存在: " + userEmail));

        // 创建候选人
        Candidate candidate = new Candidate();
        candidate.setCandidateName(createDto.getCandidateName());
        candidate.setCandidateWechat(createDto.getCandidateWechat());
        candidate.setReferredBy(referredBy);
        candidate.setStatus(CandidateStatus.PENDING);

        // 处理简历文件
        if (resumeFile != null && !resumeFile.isEmpty()) {
            try {
                candidate.setResumeFile(resumeFile.getBytes());
                candidate.setResumeFilename(resumeFile.getOriginalFilename());
                candidate.setResumeContentType(resumeFile.getContentType());
            } catch (IOException e) {
                throw new RuntimeException("简历文件上传失败: " + e.getMessage());
            }
        }

        // 保存候选人
        Candidate savedCandidate = candidateRepository.save(candidate);

        // 发送邮件通知管理员
        sendAdminNotification(savedCandidate);

        return convertToResponseDto(savedCandidate);
    }

    /**
     * 获取用户的所有候选人
     */
    public List<CandidateResponseDto> getUserCandidates(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("用户不存在: " + userEmail));

        return candidateRepository.findByReferredBy(user)
                .stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * 获取所有候选人（管理员功能）
     */
    public List<CandidateResponseDto> getAllCandidates() {
        return candidateRepository.findAll()
                .stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * 根据状态获取候选人（管理员功能）
     */
    public List<CandidateResponseDto> getCandidatesByStatus(CandidateStatus status) {
        return candidateRepository.findByStatus(status)
                .stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * 更新候选人状态（管理员功能）
     */
    public CandidateResponseDto updateCandidateStatus(Long candidateId, 
                                                    CandidateStatusUpdateDto updateDto) {
        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new RuntimeException("候选人不存在: " + candidateId));

        candidate.setStatus(updateDto.getStatus());
        candidate.setAdminComments(updateDto.getAdminComments());

        Candidate savedCandidate = candidateRepository.save(candidate);
        return convertToResponseDto(savedCandidate);
    }

    /**
     * 获取候选人简历文件
     */
    public Candidate getCandidateWithResume(Long candidateId) {
        return candidateRepository.findById(candidateId)
                .orElseThrow(() -> new RuntimeException("候选人不存在: " + candidateId));
    }

    /**
     * 获取用户推荐统计
     */
    public long getUserCandidateCount(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("用户不存在: " + userEmail));
        
        return candidateRepository.countByReferredById(user.getId());
    }

    /**
     * 发送管理员通知邮件
     */
    private void sendAdminNotification(Candidate candidate) {
        try {
            String subject = "新候选人推荐通知";
            String message = String.format(
                "用户 %s 推荐了新的候选人：\n\n" +
                "候选人姓名：%s\n" +
                "候选人微信：%s\n" +
                "推荐时间：%s\n\n" +
                "请登录系统查看详情。",
                candidate.getReferredBy().getEmail(),
                candidate.getCandidateName(),
                candidate.getCandidateWechat(),
                candidate.getCreatedAt()
            );

            // 这里应该发送给所有管理员，暂时发给固定邮箱
            emailService.sendSimpleEmail("admin@example.com", subject, message);
        } catch (Exception e) {
            // 邮件发送失败不应该影响候选人创建
            System.err.println("发送管理员通知失败: " + e.getMessage());
        }
    }

    /**
     * 转换为响应DTO
     */
    private CandidateResponseDto convertToResponseDto(Candidate candidate) {
        CandidateResponseDto dto = new CandidateResponseDto();
        dto.setId(candidate.getId());
        dto.setCandidateName(candidate.getCandidateName());
        dto.setCandidateWechat(candidate.getCandidateWechat());
        dto.setStatus(candidate.getStatus());
        dto.setReferredByEmail(candidate.getReferredBy().getEmail());
        dto.setReferredByUserId(candidate.getReferredBy().getId());
        dto.setAdminComments(candidate.getAdminComments());
        dto.setHasResume(candidate.getResumeFile() != null);
        dto.setResumeFilename(candidate.getResumeFilename());
        dto.setCreatedAt(candidate.getCreatedAt());
        dto.setUpdatedAt(candidate.getUpdatedAt());

        return dto;
    }
}
