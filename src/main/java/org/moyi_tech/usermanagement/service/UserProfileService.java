package org.moyi_tech.usermanagement.service;

import org.moyi_tech.usermanagement.constant.CandidateStatus;
import org.moyi_tech.usermanagement.dto.UserProfileDto;
import org.moyi_tech.usermanagement.dto.UserProfileUpdateDto;
import org.moyi_tech.usermanagement.entity.User;
import org.moyi_tech.usermanagement.repository.CandidateRepository;
import org.moyi_tech.usermanagement.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
@Transactional
public class UserProfileService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CandidateRepository candidateRepository;

    /**
     * Get user profile
     */
    public UserProfileDto getUserProfile(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found: " + userEmail));

        UserProfileDto profile = new UserProfileDto();
        profile.setId(user.getId());
        profile.setName(user.getEmail());        // Name = Email
        profile.setEmail(user.getEmail());
        profile.setWechatId(user.getWechatId());
        profile.setUserLevel(calculateUserLevel(user));
        profile.setOpenCandidates(getOpenCandidatesCount(user));

        return profile;
    }

    /**
     * Update user profile (can only modify WeChat ID)
     */
    public UserProfileDto updateUserProfile(String userEmail, UserProfileUpdateDto updateDto) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found: " + userEmail));

        // Only update WeChat ID
        user.setWechatId(updateDto.getWechatId());

        User savedUser = userRepository.save(user);
        return getUserProfile(savedUser.getEmail());
    }

    /**
     * Calculate user level
     * According to Feature 1.7.4: "User Level" is the number of candidates in the "Offered" status
     */
    private Integer calculateUserLevel(User user) {
        long offeredCandidates = candidateRepository.countByReferredByAndStatus(user, CandidateStatus.OFFERED);
        return Math.toIntExact(offeredCandidates);
    }

    /**
     * Get open candidates count
     * According to Feature 1.7.5: "Open Candidates" is the number of candidates in the status of "Approved", "Training", or "Marketing"
     */
    private Long getOpenCandidatesCount(User user) {
        List<CandidateStatus> openStatuses = Arrays.asList(
            CandidateStatus.APPROVED,
            CandidateStatus.TRAINING,
            CandidateStatus.MARKETING
        );

        return candidateRepository.countByReferredByAndStatusIn(user, openStatuses);
    }
}