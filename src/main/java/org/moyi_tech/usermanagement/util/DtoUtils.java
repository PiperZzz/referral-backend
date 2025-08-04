package org.moyi_tech.usermanagement.util;

import java.util.Set;
import java.util.stream.Collectors;

import org.moyi_tech.usermanagement.dto.CandidateInfoDto;
import org.moyi_tech.usermanagement.dto.UserInfoDto;
import org.moyi_tech.usermanagement.entity.User;
import org.moyi_tech.usermanagement.entity.Candidate;
import org.springframework.stereotype.Component;

@Component
public class DtoUtils {

    public static UserInfoDto convertToResponseDto(User user) {
        UserInfoDto dto = new UserInfoDto();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setWechatId(user.getWechatId());
        dto.setUsername(user.getUsername());
        dto.setReferrerWechatId(user.getReferrerWechatId());
        dto.setStatus(user.getStatus());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        
        Set<String> roleNames = user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toSet());
        dto.setRoles(roleNames);
        
        return dto;
    }

    public static CandidateInfoDto convertToAdminCandidateResponseDto(Candidate candidate) {
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

        // Admin permissions - can update status and add comments, but cannot edit basic info
        dto.setCanEdit(false); // Admins cannot edit candidate name, wechat, etc.
        dto.setCanDelete(true); // Admins can delete candidates
        
        return dto;
    }
}