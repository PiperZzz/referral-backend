package org.moyi_tech.usermanagement.service;

import org.moyi_tech.usermanagement.constant.RoleName;
import org.moyi_tech.usermanagement.constant.UserStatus;
import org.moyi_tech.usermanagement.dto.RegistrationRequest;
import org.moyi_tech.usermanagement.dto.UserInfoDto;
import org.moyi_tech.usermanagement.entity.Role;
import org.moyi_tech.usermanagement.entity.User;
import org.moyi_tech.usermanagement.repository.RoleRepository;
import org.moyi_tech.usermanagement.repository.UserRepository;
import org.moyi_tech.usermanagement.util.DtoUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserInfoDto registerUser(RegistrationRequest registrationRequest) {
        if (userRepository.existsByEmail(registrationRequest.getEmail())) {
            throw new RuntimeException("Email has been registered: " + registrationRequest.getEmail());
        }

        User user = new User();
        user.setEmail(registrationRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registrationRequest.getPassword()));
        user.setUsername(registrationRequest.getUsername());
        user.setWechatId(registrationRequest.getWechatId());
        user.setReferrerWechatId(registrationRequest.getReferrerWechatId());
        user.setStatus(UserStatus.INACTIVE); // Default status is INACTIVE

        Set<Role> roles = new HashSet<>();
        Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
                .orElseThrow(() -> new RuntimeException("Default user role not found"));
        roles.add(userRole);
        user.setRoles(roles);

        User savedUser = userRepository.save(user);

        return convertToResponseDto(savedUser);
    }

    public List<UserInfoDto> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    public Optional<UserInfoDto> findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(this::convertToResponseDto);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    } 

    public String getUserPrimaryRole(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));

        // Master > Admin > User
        if (user.getRoles().stream().anyMatch(role -> role.getName() == RoleName.ROLE_MASTER)) {
            return RoleName.ROLE_MASTER.name();
        } else if (user.getRoles().stream().anyMatch(role -> role.getName() == RoleName.ROLE_ADMIN)) {
            return RoleName.ROLE_ADMIN.name();
        } else {
            return RoleName.ROLE_USER.name();
        }
    }

    public boolean userHasRole(String email, String roleName) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));
        
        RoleName targetRole;
        try {
            targetRole = RoleName.valueOf("ROLE_" + roleName.toUpperCase());
        } catch (IllegalArgumentException e) {
            return false;
        }
        
        return user.getRoles().stream()
                .anyMatch(role -> role.getName() == targetRole);
    }

    @Transactional
    public void resetPassword(String email, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Transactional
    public UserInfoDto updateWechatInfo(String email, String wechatId, String referrerWechatId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));

        user.setWechatId(wechatId);
        user.setReferrerWechatId(referrerWechatId);

        User savedUser = userRepository.save(user);
        return convertToResponseDto(savedUser);
    }

    @Transactional
    public UserInfoDto updateUsername(String email, String username) {
    User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found: " + email));

    if (username != null && !username.trim().isEmpty()) {
        Optional<User> existingUser = userRepository.findByUsername(username);
        if (existingUser.isPresent() && !existingUser.get().getId().equals(user.getId())) {
            throw new RuntimeException("Username already exists: " + username);
        }
        user.setUsername(username.trim());
    }

    User savedUser = userRepository.save(user);
    return convertToResponseDto(savedUser);
}

    private UserInfoDto convertToResponseDto(User user) {
        return DtoUtils.convertToResponseDto(user);
    }
}
