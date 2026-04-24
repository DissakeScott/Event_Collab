package com.eventcollab.user.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class UserResponse {
    private UUID          id;
    private String        email;
    private String        firstName;
    private String        lastName;
    private String        role;
    private LocalDateTime createdAt;
}