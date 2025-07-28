package org.moyi_tech.usermanagement.service;

import org.moyi_tech.usermanagement.dto.UserProfileDto;
import org.moyi_tech.usermanagement.dto.UserProfileUpdateDto;
import org.moyi_tech.usermanagement.entity.CandidateStatus;
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
     * 获取用户个人资料
     */
    public UserProfileDto getUserProfile(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("用户不存在: " + userEmail));

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
     * 更新用户个人资料（只能修改微信号）
     */
    public UserProfileDto updateUserProfile(String userEmail, UserProfileUpdateDto updateDto) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("用户不存在: " + userEmail));

        // 只更新微信号
        user.setWechatId(updateDto.getWechatId());

        User savedUser = userRepository.save(user);
        return getUserProfile(savedUser.getEmail());
    }

    /**
     * 计算用户等级
     * 基于用户推荐的候选人数量计算等级
     */
    private Integer calculateUserLevel(User user) {
        long totalCandidates = candidateRepository.countByReferredById(user.getId());
        
        // 等级计算规则
        if (totalCandidates >= 50) {
            return 5; // 专家级
        } else if (totalCandidates >= 20) {
            return 4; // 高级
        } else if (totalCandidates >= 10) {
            return 3; // 中级
        } else if (totalCandidates >= 5) {
            return 2; // 初级
        } else {
            return 1; // 新手
        }
    }

    /**
     * 获取开放候选人数量
     * 统计状态为 APPROVED, TRAINING, MARKETING 的候选人数量
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
