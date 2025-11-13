package com.buildmaster.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    
    private Long id;
    private String username;
    private String email;
    private String displayName;
    private String avatarUrl;
    private String token; // 简单的token，后续可以改为JWT
}

