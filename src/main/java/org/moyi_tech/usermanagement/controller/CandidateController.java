package org.moyi_tech.usermanagement.controller;

import org.moyi_tech.usermanagement.dto.CandidateCreateDto;
import org.moyi_tech.usermanagement.dto.CandidateResponseDto;
import org.moyi_tech.usermanagement.entity.Candidate;
import org.moyi_tech.usermanagement.service.CandidateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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
     * 下载候选人简历
     */
    @GetMapping("/{candidateId}/resume")
    public ResponseEntity<?> downloadResume(@PathVariable Long candidateId, Principal principal) {
        try {
            Candidate candidate = candidateService.getCandidateWithResume(candidateId);

            // 检查权限：只有推荐人和管理员可以下载
            // 这里简化处理，后续可以添加更复杂的权限检查

            if (candidate.getResumeFile() == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "该候选人没有上传简历");
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
            response.put("message", e.getMessage());

            return ResponseEntity.badRequest().body(response);
        }
    }
}
