package com.eventcollab.user.controller;

import com.eventcollab.common.dto.ApiResponse;
import com.eventcollab.common.exception.BusinessException;
import com.eventcollab.user.dto.UserResponse;
import com.eventcollab.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @GetMapping("/me")
    public ApiResponse<UserResponse> me(@AuthenticationPrincipal UserDetails principal) {
        return userRepository.findByEmail(principal.getUsername())
                .map(u -> ApiResponse.ok(UserResponse.builder()
                        .id(u.getId()).email(u.getEmail())
                        .firstName(u.getFirstName()).lastName(u.getLastName())
                        .role(u.getRole().name()).createdAt(u.getCreatedAt())
                        .build()))
                .orElseThrow(() -> BusinessException.notFound("Utilisateur introuvable"));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<UserResponse>> listAll() {
        List<UserResponse> users = userRepository.findAll().stream()
                .map(u -> UserResponse.builder()
                        .id(u.getId()).email(u.getEmail())
                        .firstName(u.getFirstName()).lastName(u.getLastName())
                        .role(u.getRole().name()).createdAt(u.getCreatedAt())
                        .build())
                .toList();
        return ApiResponse.ok(users);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<UserResponse> getById(@PathVariable UUID id) {
        return userRepository.findById(id)
                .map(u -> ApiResponse.ok(UserResponse.builder()
                        .id(u.getId()).email(u.getEmail())
                        .firstName(u.getFirstName()).lastName(u.getLastName())
                        .role(u.getRole().name()).createdAt(u.getCreatedAt())
                        .build()))
                .orElseThrow(() -> BusinessException.notFound("Utilisateur introuvable : " + id));
    }
}