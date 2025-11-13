package com.buildmaster.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserDTO {
    
    private Long id;
    private String username;
    private String email;
    private String displayName;
    private String avatarUrl;
    private LocalDateTime createdAt;
}

