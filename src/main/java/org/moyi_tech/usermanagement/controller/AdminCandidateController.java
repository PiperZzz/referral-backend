package org.moyi_tech.usermanagement.controller;

import org.moyi_tech.usermanagement.dto.CandidateResponseDto;
import org.moyi_tech.usermanagement.dto.CandidateStatusUpdateDto;
import org.moyi_tech.usermanagement.entity.CandidateStatus;
import org.moyi_tech.usermanagement.service.CandidateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/candidates")
@Validated
public class AdminCandidateController {

    @Autowired
    private CandidateService candidateService;

    /**
     * 获取所有候选人（管理员功能）
     */
    @GetMapping
    public ResponseEntity<?> getAllCandidates() {
        try {
            List<CandidateResponseDto> candidates = candidateService.getAllCandidates();

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
     * 根据状态获取候选人
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<?> getCandidatesByStatus(@PathVariable CandidateStatus status) {
        try {
            List<CandidateResponseDto> candidates = candidateService.getCandidatesByStatus(status);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("candidates", candidates);
            response.put("total", candidates.size());
            response.put("status", status);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());

            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 更新候选人状态
     */
    @PutMapping("/{candidateId}/status")
    public ResponseEntity<?> updateCandidateStatus(
            @PathVariable Long candidateId,
            @Valid @RequestBody CandidateStatusUpdateDto updateDto) {
        
        try {
            CandidateResponseDto candidate = candidateService.updateCandidateStatus(candidateId, updateDto);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "候选人状态更新成功");
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
     * 获取待审核候选人（快捷方法）
     */
    @GetMapping("/pending")
    public ResponseEntity<?> getPendingCandidates() {
        return getCandidatesByStatus(CandidateStatus.PENDING);
    }

    /**
     * 批量审核候选人
     */
    @PutMapping("/batch-approve")
    public ResponseEntity<?> batchApproveCandidates(@RequestBody List<Long> candidateIds) {
        try {
            Map<String, Object> results = new HashMap<>();
            int successCount = 0;
            
            for (Long candidateId : candidateIds) {
                try {
                    CandidateStatusUpdateDto updateDto = new CandidateStatusUpdateDto();
                    updateDto.setStatus(CandidateStatus.APPROVED);
                    updateDto.setAdminComments("批量审核通过");
                    
                    candidateService.updateCandidateStatus(candidateId, updateDto);
                    successCount++;
                } catch (Exception e) {
                    // 记录失败的候选人ID，但继续处理其他的
                    results.put("failed_" + candidateId, e.getMessage());
                }
            }

            results.put("success", true);
            results.put("message", String.format("批量操作完成，成功处理 %d/%d 个候选人", 
                                                successCount, candidateIds.size()));
            results.put("processedCount", successCount);
            results.put("totalCount", candidateIds.size());

            return ResponseEntity.ok(results);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());

            return ResponseEntity.badRequest().body(response);
        }
    }
}
