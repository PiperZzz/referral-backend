package org.moyi_tech.usermanagement.service;

import org.moyi_tech.usermanagement.constant.CandidateStatus;
import org.moyi_tech.usermanagement.constant.UserStatus;
import org.moyi_tech.usermanagement.dto.CandidateCreateDto;
import org.moyi_tech.usermanagement.dto.CandidateInfoDto;
import org.moyi_tech.usermanagement.dto.CandidateStatusUpdateDto;
import org.moyi_tech.usermanagement.dto.CandidateUpdateDto;
import org.moyi_tech.usermanagement.entity.Candidate;
import org.moyi_tech.usermanagement.entity.RoleName;
import org.moyi_tech.usermanagement.entity.User;
import org.moyi_tech.usermanagement.repository.CandidateRepository;
import org.moyi_tech.usermanagement.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    public CandidateInfoDto createCandidate(CandidateCreateDto createDto, 
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
    public List<CandidateInfoDto> getUserCandidates(String userEmail) {
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
    public List<CandidateInfoDto> getAllCandidates() {
        return candidateRepository.findAll()
                .stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * 根据状态获取候选人（管理员功能）
     */
    public List<CandidateInfoDto> getCandidatesByStatus(CandidateStatus status) {
        return candidateRepository.findByStatus(status)
                .stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * 更新候选人状态（管理员功能）
     */
    public CandidateInfoDto updateCandidateStatus(Long candidateId, 
                                                    CandidateStatusUpdateDto updateDto) {
        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new RuntimeException("候选人不存在: " + candidateId));

        candidate.setStatus(updateDto.getStatus());
        candidate.setAdminComments(updateDto.getAdminComments());

        Candidate savedCandidate = candidateRepository.save(candidate);
        return convertToResponseDto(savedCandidate);
    }

    /**
     * 获取单个候选人详情（用户只能查看自己推荐的）
     */
    public CandidateInfoDto getCandidateById(Long candidateId, String userEmail) {
        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new RuntimeException("候选人不存在: " + candidateId));

        // 验证权限：只能查看自己推荐的候选人
        if (!candidate.getReferredBy().getEmail().equals(userEmail)) {
            throw new RuntimeException("无权限查看此候选人信息");
        }

        return convertToResponseDto(candidate);
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
    private CandidateInfoDto convertToResponseDto(Candidate candidate) {
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

        // Set edit permissions (screening status can be edited)
        dto.setCanEdit(candidate.getStatus() == CandidateStatus.SCREENING);
        dto.setCanDelete(candidate.getStatus() == CandidateStatus.SCREENING);

        return dto;
    }

    /**
     * Convert to response DTO (with user-specific permissions)
     */
    private CandidateInfoDto convertToResponseDto(Candidate candidate, String currentUserEmail) {
        CandidateInfoDto dto = convertToResponseDto(candidate);
        
        // Only referrer can edit their own recommended candidates
        boolean isOwner = candidate.getReferredBy().getEmail().equals(currentUserEmail);
        dto.setCanEdit(dto.isCanEdit() && isOwner);
        dto.setCanDelete(dto.isCanDelete() && isOwner);

        return dto;
    }

    /**
     * Update candidate resume
     */
    public CandidateInfoDto updateCandidateResume(Long candidateId, 
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

    /**
     * Get candidate with resume (for download)
     */
    public Candidate getCandidateWithResume(Long candidateId) {
        return candidateRepository.findById(candidateId)
                .orElseThrow(() -> new RuntimeException("Candidate not found: " + candidateId));
    }

    /**
     * Check if user can view candidate resume
     * Users can view their own candidates' resumes, admins can view all
     */
    public boolean canViewCandidateResume(Long candidateId, String userEmail) {
        Candidate candidate = candidateRepository.findById(candidateId)
                .orElse(null);
        
        if (candidate == null) {
            return false;
        }

        // Check if user is the referrer
        if (candidate.getReferredBy().getEmail().equals(userEmail)) {
            return true;
        }

        // Check if user is admin or master
        User user = userRepository.findByEmail(userEmail).orElse(null);
        if (user != null) {
            return user.getRoles().stream()
                    .anyMatch(role -> role.getName() == RoleName.ROLE_ADMIN || 
                                    role.getName() == RoleName.ROLE_MASTER);
        }

        return false;
    }

    /**
     * Get candidate resume information
     */
    public Map<String, Object> getCandidateResumeInfo(Long candidateId, String userEmail) {
        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new RuntimeException("Candidate not found: " + candidateId));

        // Check permissions
        if (!canViewCandidateResume(candidateId, userEmail)) {
            throw new RuntimeException("No permission to view this candidate's resume");
        }

        Map<String, Object> resumeInfo = new HashMap<>();
        resumeInfo.put("hasResume", candidate.getResumeFile() != null);
        resumeInfo.put("filename", candidate.getResumeFilename());
        resumeInfo.put("contentType", candidate.getResumeContentType());
        resumeInfo.put("uploadDate", candidate.getUpdatedAt());
        resumeInfo.put("candidateId", candidateId);
        resumeInfo.put("candidateName", candidate.getCandidateName());

        return resumeInfo;
    }

    /**
     * Get candidate permissions for current user
     */
    public Map<String, Boolean> getCandidatePermissions(Long candidateId, String userEmail) {
        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new RuntimeException("Candidate not found: " + candidateId));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found: " + userEmail));

        Map<String, Boolean> permissions = new HashMap<>();
        
        // Check if user is the referrer
        boolean isOwner = candidate.getReferredBy().getEmail().equals(userEmail);
        
        // Check if user is admin or master
        boolean isAdmin = user.getRoles().stream()
                .anyMatch(role -> role.getName() == RoleName.ROLE_ADMIN || 
                                role.getName() == RoleName.ROLE_MASTER);

        // Set permissions based on role and ownership
        permissions.put("canView", isOwner || isAdmin);
        permissions.put("canEdit", isOwner && candidate.getStatus() == CandidateStatus.SCREENING);
        permissions.put("canDelete", isOwner && candidate.getStatus() == CandidateStatus.SCREENING);
        permissions.put("canDownloadResume", isOwner || isAdmin);
        permissions.put("canUpdateStatus", isAdmin);
        permissions.put("canAddComments", isAdmin);
        permissions.put("isOwner", isOwner);
        permissions.put("isAdmin", isAdmin);

        return permissions;
    }

    /**
     * Send admin notification when new candidate is submitted
     * For Feature 1.3.2 - Email notification to all admins
     */
    private void sendAdminNotification(Candidate candidate) {
        try {
            // Get all active admin users
            List<User> admins = userRepository.findByRoleName(RoleName.ROLE_ADMIN)
                    .stream()
                    .filter(admin -> admin.getStatus() == UserStatus.ACTIVE)
                    .collect(Collectors.toList());

            String subject = "New Candidate Referral Submitted";
            String message = String.format(
                "A new candidate has been submitted:\n\n" +
                "Candidate Name: %s\n" +
                "Submitted by: %s (%s)\n" +
                "Submission Date: %s\n\n" +
                "Please review the candidate in the admin portal.",
                candidate.getCandidateName(),
                candidate.getReferredBy().getEmail(),
                candidate.getReferredBy().getEmail(),
                candidate.getCreatedAt()
            );

            // Send email to each admin
            for (User admin : admins) {
                try {
                    emailService.sendSimpleEmail(admin.getEmail(), subject, message);
                } catch (Exception e) {
                    System.err.println("Failed to send notification to admin: " + admin.getEmail() + 
                                     ", Error: " + e.getMessage());
                }
            }

        } catch (Exception e) {
            System.err.println("Failed to send admin notifications: " + e.getMessage());
            // Don't throw exception here as it's not critical for candidate creation
        }
    }

    /**
     * Validate resume file type
     */
    private boolean isValidResumeType(String contentType) {
        return contentType.equals("application/pdf") ||
               contentType.equals("application/msword") ||
               contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document") ||
               contentType.equals("text/plain") ||
               contentType.startsWith("text/");
    }

    /**
     * Get candidate for editing (with permission check)
     * For Feature 1.4.3 - Edit Referral overlay
     */
    public CandidateInfoDto getCandidateForEdit(Long candidateId, String userEmail) {
        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new RuntimeException("Candidate not found: " + candidateId));

        // Verify permissions: only referrer can edit their own candidates
        if (!candidate.getReferredBy().getEmail().equals(userEmail)) {
            throw new RuntimeException("No permission to edit this candidate");
        }

        // Check if candidate is in editable state (SCREENING)
        if (candidate.getStatus() != CandidateStatus.SCREENING) {
            throw new RuntimeException("Can only edit candidates in screening status");
        }

        return convertToResponseDto(candidate, userEmail);
    }

    /**
     * Update candidate basic information
     * For Feature 1.4.3 - Edit candidate name and WeChat
     */
    public CandidateInfoDto updateCandidate(Long candidateId, 
                                              CandidateUpdateDto updateDto, 
                                              String userEmail) {
        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new RuntimeException("Candidate not found: " + candidateId));

        // Verify permissions: only referrer can edit their own candidates
        if (!candidate.getReferredBy().getEmail().equals(userEmail)) {
            throw new RuntimeException("No permission to edit this candidate");
        }

        // Check if candidate is in editable state
        if (candidate.getStatus() != CandidateStatus.SCREENING) {
            throw new RuntimeException("Can only edit candidates in screening status");
        }

        // Check uniqueness of WeChat ID if changed
        if (!candidate.getCandidateWechat().equals(updateDto.getCandidateWechat())) {
            if (candidateRepository.existsByCandidateWechat(updateDto.getCandidateWechat())) {
                throw new RuntimeException("Candidate WeChat already exists: " + updateDto.getCandidateWechat());
            }
        }

        // Update basic information
        candidate.setCandidateName(updateDto.getCandidateName());
        candidate.setCandidateWechat(updateDto.getCandidateWechat());

        Candidate savedCandidate = candidateRepository.save(candidate);
        return convertToResponseDto(savedCandidate, userEmail);
    }

    /**
     * Get user candidate count
     */
    public long getUserCandidateCount(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found: " + userEmail));

        return candidateRepository.countByReferredById(user.getId());
    }
}
