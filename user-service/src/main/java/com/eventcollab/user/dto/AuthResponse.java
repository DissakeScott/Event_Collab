package com.eventcollab.user.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthResponse {
    private String       accessToken;
    private String       refreshToken;
    private long         expiresIn;
    private UserResponse user;
}