package com.buildmaster.controller;

import com.buildmaster.dto.*;
import com.buildmaster.model.User;
import com.buildmaster.service.EmailService;
import com.buildmaster.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Tag(name = "用户管理", description = "用户注册、登录等接口")
public class UserController {

    private final EmailService emailService;
    private final UserService userService;

    /**
     * 发送注册验证码
     */
    @PostMapping("/send-code")
    @Operation(summary = "发送注册验证码", description = "向指定邮箱发送6位数字验证码，有效期5分钟")
    public ApiResponse<Void> sendVerificationCode(@Valid @RequestBody SendCodeRequest request) {
        try {
            emailService.sendVerificationCode(request.getEmail());
            return ApiResponse.success("验证码已发送，请查收邮箱", null);
        } catch (MessagingException e) {
            log.error("Failed to send verification code to {}", request.getEmail(), e);
            return ApiResponse.error("验证码发送失败，请稍后重试");
        }
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "使用邮箱验证码完成用户注册")
    public ApiResponse<UserDTO> register(@Valid @RequestBody RegisterRequest request) {
        try {
            UserDTO user = userService.register(request);
            return ApiResponse.success("注册成功", user);
        } catch (IllegalArgumentException e) {
            log.warn("Registration failed: {}", e.getMessage());
            return ApiResponse.error(e.getMessage());
        } catch (Exception e) {
            log.error("Registration error", e);
            return ApiResponse.error("注册失败，请稍后重试");
        }
    }

    /**
     * 检查用户名是否可用
     */
    @GetMapping("/check-username")
    @Operation(summary = "检查用户名是否可用")
    public ApiResponse<Boolean> checkUsername(@RequestParam String username) {
        // 这里可以调用 userRepository.findByUsername(username).isEmpty()
        // 暂时先返回简单实现
        return ApiResponse.success(true);
    }

    /**
     * 检查邮箱是否可用
     */
    @GetMapping("/check-email")
    @Operation(summary = "检查邮箱是否可用")
    public ApiResponse<Boolean> checkEmail(@RequestParam String email) {
        // 这里可以调用 userRepository.findByEmail(email).isEmpty()
        // 暂时先返回简单实现
        return ApiResponse.success(true);
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "使用邮箱和密码登录")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            User user = userService.login(request.getEmail(), request.getPassword());
            
            // 生成简单的token（实际应用中应该使用JWT）
            String token = java.util.UUID.randomUUID().toString();
            
            LoginResponse response = new LoginResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getDisplayName(),
                user.getAvatarUrl(),
                token
            );
            
            return ApiResponse.success("登录成功", response);
        } catch (IllegalArgumentException e) {
            log.warn("Login failed: {}", e.getMessage());
            return ApiResponse.error(e.getMessage());
        } catch (Exception e) {
            log.error("Login error", e);
            return ApiResponse.error("登录失败，请稍后重试");
        }
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/update")
    @Operation(summary = "更新用户信息", description = "更新用户名、显示名称、头像等")
    public ApiResponse<UserDTO> updateUser(@RequestParam Long userId,
                                            @RequestParam(required = false) String username,
                                            @RequestParam(required = false) String displayName,
                                            @RequestParam(required = false) String avatarUrl) {
        try {
            User user = userService.updateUserInfo(userId, username, displayName, avatarUrl);
            
            UserDTO userDTO = new UserDTO();
            userDTO.setId(user.getId());
            userDTO.setUsername(user.getUsername());
            userDTO.setEmail(user.getEmail());
            userDTO.setDisplayName(user.getDisplayName());
            userDTO.setAvatarUrl(user.getAvatarUrl());
            userDTO.setCreatedAt(user.getCreatedAt());
            
            return ApiResponse.success("更新成功", userDTO);
        } catch (IllegalArgumentException e) {
            log.warn("Update user failed: {}", e.getMessage());
            return ApiResponse.error(e.getMessage());
        } catch (Exception e) {
            log.error("Update user error", e);
            return ApiResponse.error("更新失败，请稍后重试");
        }
    }

    /**
     * 修改密码
     */
    @PutMapping("/change-password")
    @Operation(summary = "修改密码")
    public ApiResponse<Void> changePassword(@RequestParam Long userId,
                                             @RequestParam String currentPassword,
                                             @RequestParam String newPassword) {
        try {
            userService.changePassword(userId, currentPassword, newPassword);
            return ApiResponse.success("密码修改成功", null);
        } catch (IllegalArgumentException e) {
            log.warn("Change password failed: {}", e.getMessage());
            return ApiResponse.error(e.getMessage());
        } catch (Exception e) {
            log.error("Change password error", e);
            return ApiResponse.error("密码修改失败，请稍后重试");
        }
    }
}

