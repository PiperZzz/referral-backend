package org.moyi_tech.usermanagement.controller;

import org.moyi_tech.usermanagement.dto.CandidateCreateDto;
import org.moyi_tech.usermanagement.dto.CandidateResponseDto;
import org.moyi_tech.usermanagement.dto.CandidateUpdateDto;
import org.moyi_tech.usermanagement.entity.Candidate;
import org.moyi_tech.usermanagement.service.CandidateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/candidates")
@Validated
public class CandidateController {

    @Autowired
    private CandidateService candidateService;

    /**
     * 创建新候选人（用户功能）
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createCandidate(
            @RequestParam("candidateName") String candidateName,
            @RequestParam("candidateWechat") String candidateWechat,
            @RequestParam(value = "resumeFile", required = false) MultipartFile resumeFile,
            Principal principal) {
        
        try {
            CandidateCreateDto createDto = new CandidateCreateDto();
            createDto.setCandidateName(candidateName);
            createDto.setCandidateWechat(candidateWechat);

            CandidateResponseDto candidate = candidateService.createCandidate(
                createDto, resumeFile, principal.getName());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "候选人推荐成功");
            response.put("candidate", candidate);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());

            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取当前用户的候选人列表
     */
    @GetMapping("/my-candidates")
    public ResponseEntity<?> getMyCandidates(Principal principal) {
        try {
            List<CandidateResponseDto> candidates = candidateService.getUserCandidates(principal.getName());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("candidates", candidates);
            response.put("total", candidates.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());

            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取用户推荐统计
     */
    @GetMapping("/my-stats")
    public ResponseEntity<?> getMyStats(Principal principal) {
        try {
            long totalCount = candidateService.getUserCandidateCount(principal.getName());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("totalReferred", totalCount);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());

            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取单个候选人详情
     */
    @GetMapping("/{candidateId}")
    public ResponseEntity<?> getCandidateById(@PathVariable Long candidateId, Principal principal) {
        try {
            CandidateResponseDto candidate = candidateService.getCandidateById(candidateId, principal.getName());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("candidate", candidate);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());

            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 更新候选人基本信息
     */
    @PutMapping("/{candidateId}")
    public ResponseEntity<?> updateCandidate(
            @PathVariable Long candidateId,
            @RequestParam("candidateName") String candidateName,
            @RequestParam("candidateWechat") String candidateWechat,
            Principal principal) {
        
        try {
            CandidateUpdateDto updateDto = new CandidateUpdateDto();
            updateDto.setCandidateName(candidateName);
            updateDto.setCandidateWechat(candidateWechat);

            CandidateResponseDto candidate = candidateService.updateCandidate(
                candidateId, updateDto, principal.getName());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "候选人信息更新成功");
            response.put("candidate", candidate);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());

            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 删除候选人
     */
    @DeleteMapping("/{candidateId}")
    public ResponseEntity<?> deleteCandidate(@PathVariable Long candidateId, Principal principal) {
        try {
            candidateService.deleteCandidate(candidateId, principal.getName());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "候选人删除成功");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());

            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 检查是否可以编辑候选人
     */
    @GetMapping("/{candidateId}/editable")
    public ResponseEntity<?> checkEditPermission(@PathVariable Long candidateId, Principal principal) {
        try {
            boolean canEdit = candidateService.canEditCandidate(candidateId, principal.getName());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("canEdit", canEdit);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());

            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Download candidate resume
     * For Feature 1.4.4 and admin resume review
     */
    @GetMapping("/{candidateId}/resume/download")
    public ResponseEntity<?> downloadResume(@PathVariable Long candidateId, Principal principal) {
        try {
            // Get candidate with resume
            Candidate candidate = candidateService.getCandidateWithResume(candidateId);
            
            // Check permissions: only referrer and admins can download
            String userEmail = principal.getName();
            if (!candidateService.canViewCandidateResume(candidateId, userEmail)) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "No permission to download this resume");
                return ResponseEntity.status(403).body(response);
            }

            if (candidate.getResumeFile() == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "No resume file found for this candidate");
                return ResponseEntity.badRequest().body(response);
            }

            ByteArrayResource resource = new ByteArrayResource(candidate.getResumeFile());

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                           "attachment; filename=\"" + candidate.getResumeFilename() + "\"")
                    .contentType(MediaType.parseMediaType(candidate.getResumeContentType()))
                    .contentLength(candidate.getResumeFile().length)
                    .body(resource);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to download resume: " + e.getMessage());

            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Get candidate resume info (filename, upload date)
     * For Feature 1.4.4 - Resume field shows filename and upload date
     */
    @GetMapping("/{candidateId}/resume/info")
    public ResponseEntity<?> getResumeInfo(@PathVariable Long candidateId, Principal principal) {
        try {
            Map<String, Object> resumeInfo = candidateService.getCandidateResumeInfo(candidateId, principal.getName());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("resumeInfo", resumeInfo);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to get resume info: " + e.getMessage());

            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Check if user can edit candidate
     * For frontend permission checks
     */
    @GetMapping("/{candidateId}/permissions")
    public ResponseEntity<?> getCandidatePermissions(@PathVariable Long candidateId, Principal principal) {
        try {
            Map<String, Boolean> permissions = candidateService.getCandidatePermissions(candidateId, principal.getName());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("permissions", permissions);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to get candidate permissions: " + e.getMessage());

            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Update candidate basic information
     * For Feature 1.4.3 - Edit candidate name and WeChat
     */
    @PutMapping("/{candidateId}")
    public ResponseEntity<?> updateCandidate(
            @PathVariable Long candidateId,
            @Valid @RequestBody CandidateUpdateDto updateDto,
            Principal principal) {
        
        try {
            CandidateResponseDto candidate = candidateService.updateCandidate(
                candidateId, updateDto, principal.getName());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Candidate updated successfully");
            response.put("candidate", candidate);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to update candidate: " + e.getMessage());

            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Update candidate resume only
     * For Feature 1.4.3 - Reupload resume
     */
    @PutMapping("/{candidateId}/resume")
    public ResponseEntity<?> updateCandidateResume(
            @PathVariable Long candidateId,
            @RequestParam("resumeFile") MultipartFile resumeFile,
            Principal principal) {
        
        try {
            CandidateResponseDto candidate = candidateService.updateCandidateResume(
                candidateId, resumeFile, principal.getName());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Resume updated successfully");
            response.put("candidate", candidate);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to update resume: " + e.getMessage());

            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Get candidate edit form data
     * For Feature 1.4.3 - Pre-populate edit form
     */
    @GetMapping("/{candidateId}/edit")
    public ResponseEntity<?> getCandidateForEdit(@PathVariable Long candidateId, Principal principal) {
        try {
            CandidateResponseDto candidate = candidateService.getCandidateForEdit(candidateId, principal.getName());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("candidate", candidate);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to get candidate for edit: " + e.getMessage());

            return ResponseEntity.badRequest().body(response);
        }
    }
}