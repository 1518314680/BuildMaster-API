package com.buildmaster.service;

import com.buildmaster.dto.RegisterRequest;
import com.buildmaster.dto.UserDTO;
import com.buildmaster.model.User;
import com.buildmaster.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final EmailService emailService;

    /**
     * 用户注册
     */
    @Transactional
    public UserDTO register(RegisterRequest request) {
        // 验证验证码
        if (!emailService.verifyCode(request.getEmail(), request.getVerificationCode())) {
            throw new IllegalArgumentException("验证码错误或已过期");
        }

        // 检查用户名是否已存在
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new IllegalArgumentException("用户名已存在");
        }

        // 检查邮箱是否已存在
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("邮箱已被注册");
        }

        // 创建新用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPasswordHash(hashPassword(request.getPassword()));
        user.setDisplayName(request.getDisplayName() != null ? request.getDisplayName() : request.getUsername());

        // 保存用户
        user = userRepository.save(user);
        log.info("User registered successfully: {}", user.getUsername());

        // 转换为DTO返回
        return convertToDTO(user);
    }

    /**
     * 根据邮箱查找用户
     */
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
    }

    /**
     * 根据用户名查找用户
     */
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
    }

    /**
     * 将User实体转换为UserDTO
     */
    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setDisplayName(user.getDisplayName());
        dto.setAvatarUrl(user.getAvatarUrl());
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }

    /**
     * 用户登录
     */
    public User login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("邮箱或密码错误"));
        
        // 验证密码
        String hashedPassword = hashPassword(password);
        if (!hashedPassword.equals(user.getPasswordHash())) {
            throw new IllegalArgumentException("邮箱或密码错误");
        }
        
        log.info("User logged in: {}", user.getUsername());
        return user;
    }

    /**
     * 更新用户信息
     */
    @Transactional
    public User updateUserInfo(Long userId, String username, String displayName, String avatarUrl) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));

        if (username != null && !username.isEmpty()) {
            // 检查用户名是否已被其他用户使用
            userRepository.findByUsername(username).ifPresent(existingUser -> {
                if (!existingUser.getId().equals(userId)) {
                    throw new IllegalArgumentException("用户名已被使用");
                }
            });
            user.setUsername(username);
        }

        if (displayName != null) {
            user.setDisplayName(displayName);
        }

        if (avatarUrl != null) {
            user.setAvatarUrl(avatarUrl);
        }

        user = userRepository.save(user);
        log.info("User info updated: {}", user.getUsername());
        return user;
    }

    /**
     * 修改密码
     */
    @Transactional
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));

        // 验证当前密码
        String hashedCurrentPassword = hashPassword(currentPassword);
        if (!hashedCurrentPassword.equals(user.getPasswordHash())) {
            throw new IllegalArgumentException("当前密码错误");
        }

        // 更新密码
        user.setPasswordHash(hashPassword(newPassword));
        userRepository.save(user);
        log.info("Password changed for user: {}", user.getUsername());
    }

    /**
     * 使用SHA-256加密密码
     */
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            log.error("Password hashing failed", e);
            throw new RuntimeException("密码加密失败");
        }
    }
}

