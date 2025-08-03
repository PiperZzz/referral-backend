package org.moyi_tech.usermanagement.controller;

import org.moyi_tech.usermanagement.dto.CandidateInfoDto;
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
     * Get all candidates (Admin function)
     */
    @GetMapping
    public ResponseEntity<?> getAllCandidates() {
        try {
            List<CandidateInfoDto> candidates = candidateService.getAllCandidates();

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
     * Get candidates by status
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<?> getCandidatesByStatus(@PathVariable CandidateStatus status) {
        try {
            List<CandidateInfoDto> candidates = candidateService.getCandidatesByStatus(status);

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
     * Update candidate status
     */
    @PutMapping("/{candidateId}/status")
    public ResponseEntity<?> updateCandidateStatus(
            @PathVariable Long candidateId,
            @Valid @RequestBody CandidateStatusUpdateDto updateDto) {
        
        try {
            CandidateInfoDto candidate = candidateService.updateCandidateStatus(candidateId, updateDto);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Candidate status updated successfully");
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
     * Get screening candidates (shortcut method)
     */
    @GetMapping("/screening")
    public ResponseEntity<?> getScreeningCandidates() {
        return getCandidatesByStatus(CandidateStatus.SCREENING);
    }

    /**
     * Batch approve candidates
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
                    updateDto.setAdminComments("Batch approved");
                    
                    candidateService.updateCandidateStatus(candidateId, updateDto);
                    successCount++;
                } catch (Exception e) {
                    // Record failed candidate IDs but continue processing others
                    results.put("failed_" + candidateId, e.getMessage());
                }
            }

            results.put("success", true);
            results.put("message", String.format("Batch operation completed, successfully processed %d/%d candidates", 
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

    /**
     * Delete candidate (Admin function)
     */
    @DeleteMapping("/{candidateId}")
    public ResponseEntity<?> deleteCandidate(@PathVariable Long candidateId) {
        try {
            candidateService.deleteCandidateByAdmin(candidateId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Candidate deleted successfully");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());

            return ResponseEntity.badRequest().body(response);
        }
    }
}