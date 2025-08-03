package org.moyi_tech.usermanagement.service;

import org.moyi_tech.usermanagement.dto.CandidateCreateDto;
import org.moyi_tech.usermanagement.dto.CandidateResponseDto;
import org.moyi_tech.usermanagement.dto.CandidateStatusUpdateDto;
import org.moyi_tech.usermanagement.dto.CandidateUpdateDto;
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
        candidate.setStatus(CandidateStatus.SCREENING); // 默认状态为筛选中

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
     * 获取单个候选人详情（用户只能查看自己推荐的）
     */
    public CandidateResponseDto getCandidateById(Long candidateId, String userEmail) {
        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new RuntimeException("候选人不存在: " + candidateId));

        // 验证权限：只能查看自己推荐的候选人
        if (!candidate.getReferredBy().getEmail().equals(userEmail)) {
            throw new RuntimeException("无权限查看此候选人信息");
        }

        return convertToResponseDto(candidate);
    }

    /**
     * 验证简历文件类型
     */
    private boolean isValidResumeType(String contentType) {
        return contentType.equals("application/pdf") ||
               contentType.equals("application/msword") ||
               contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document") ||
               contentType.equals("text/plain") ||
               contentType.startsWith("text/");
    }

/**
     * Check if user can edit candidate
     */
    public boolean canEditCandidate(Long candidateId, String userEmail) {
        return candidateRepository.findById(candidateId)
                .map(candidate -> candidate.getReferredBy().getEmail().equals(userEmail))
                .orElse(false);
    }

    /**
     * Convert to response DTO (with edit permission info)
     */
    private CandidateResponseDto convertToResponseDto(Candidate candidate) {
        CandidateResponseDto dto = new CandidateResponseDto();
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

        // Set edit permissions (screening status can be edited)
        dto.setCanEdit(candidate.getStatus() == CandidateStatus.SCREENING);
        dto.setCanDelete(candidate.getStatus() == CandidateStatus.SCREENING);

        return dto;
    }

    /**
     * Convert to response DTO (with user-specific permissions)
     */
    private CandidateResponseDto convertToResponseDto(Candidate candidate, String currentUserEmail) {
        CandidateResponseDto dto = convertToResponseDto(candidate);
        
        // Only referrer can edit their own recommended candidates
        boolean isOwner = candidate.getReferredBy().getEmail().equals(currentUserEmail);
        dto.setCanEdit(dto.isCanEdit() && isOwner);
        dto.setCanDelete(dto.isCanDelete() && isOwner);

        return dto;
    }

    /**
     * Update candidate basic information
     */
    public CandidateResponseDto updateCandidate(Long candidateId, 
                                              CandidateUpdateDto updateDto, 
                                              String userEmail) {
        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new RuntimeException("Candidate not found: " + candidateId));

        // Verify permissions: can only modify own recommended candidates
        if (!candidate.getReferredBy().getEmail().equals(userEmail)) {
            throw new RuntimeException("No permission to modify this candidate");
        }

        // Check uniqueness of WeChat ID changes
        if (!candidate.getCandidateWechat().equals(updateDto.getCandidateWechat())) {
            if (candidateRepository.existsByCandidateWechat(updateDto.getCandidateWechat())) {
                throw new RuntimeException("Candidate WeChat already exists: " + updateDto.getCandidateWechat());
            }
        }

        // Update basic information
        candidate.setCandidateName(updateDto.getCandidateName());
        candidate.setCandidateWechat(updateDto.getCandidateWechat());

        // If candidate has been reviewed, reset to screening status after modification
        if (candidate.getStatus() != CandidateStatus.SCREENING) {
            candidate.setStatus(CandidateStatus.SCREENING);
            candidate.setAdminComments("Candidate information updated, requires re-screening");
        }

        Candidate savedCandidate = candidateRepository.save(candidate);
        return convertToResponseDto(savedCandidate);
    }

    /**
     * Update candidate resume
     */
    public CandidateResponseDto updateCandidateResume(Long candidateId, 
                                                    MultipartFile resumeFile, 
                                                    String userEmail) {
        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new RuntimeException("Candidate not found: " + candidateId));

        // Verify permissions
        if (!candidate.getReferredBy().getEmail().equals(userEmail)) {
            throw new RuntimeException("No permission to modify this candidate");
        }

        // Validate file
        if (resumeFile == null || resumeFile.isEmpty()) {
            throw new RuntimeException("Resume file cannot be empty");
        }

        // Validate file type (optional)
        String contentType = resumeFile.getContentType();
        if (contentType != null && !isValidResumeType(contentType)) {
            throw new RuntimeException("Unsupported file type, please upload PDF, DOC, DOCX or TXT files");
        }

        // Validate file size (max 10MB)
        if (resumeFile.getSize() > 10 * 1024 * 1024) {
            throw new RuntimeException("File size cannot exceed 10MB");
        }

        try {
            // Update resume file
            candidate.setResumeFile(resumeFile.getBytes());
            candidate.setResumeFilename(resumeFile.getOriginalFilename());
            candidate.setResumeContentType(resumeFile.getContentType());

            // If candidate has been reviewed, reset to screening status after uploading new resume
            if (candidate.getStatus() != CandidateStatus.SCREENING) {
                candidate.setStatus(CandidateStatus.SCREENING);
                candidate.setAdminComments("Candidate resume updated, requires re-screening");
            }

            Candidate savedCandidate = candidateRepository.save(candidate);
            return convertToResponseDto(savedCandidate);

        } catch (IOException e) {
            throw new RuntimeException("Resume file upload failed: " + e.getMessage());
        }
    }

    /**
     * Delete candidate (users can delete their own recommended candidates)
     */
    public void deleteCandidate(Long candidateId, String userEmail) {
        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new RuntimeException("Candidate not found: " + candidateId));

        // Verify permissions
        if (!candidate.getReferredBy().getEmail().equals(userEmail)) {
            throw new RuntimeException("No permission to delete this candidate");
        }

        // Only allow deletion of candidates in screening status
        if (candidate.getStatus() != CandidateStatus.SCREENING) {
            throw new RuntimeException("Can only delete candidates in screening status");
        }

        candidateRepository.delete(candidate);
    }

    /**
     * Delete candidate (Admin function)
     */
    public void deleteCandidateByAdmin(Long candidateId) {
        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new RuntimeException("Candidate not found: " + candidateId));

        candidateRepository.delete(candidate);
    }
}
