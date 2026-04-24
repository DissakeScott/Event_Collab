package com.eventcollab.user.service;

import com.eventcollab.common.exception.BusinessException;
import com.eventcollab.user.domain.*;
import com.eventcollab.user.dto.*;
import com.eventcollab.user.repository.*;
import com.eventcollab.user.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository         userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder        passwordEncoder;
    private final JwtService             jwtService;
    private final AuthenticationManager  authManager;

    @Value("${app.jwt.access-token-expiration-ms}")
    private long accessExpirationMs;

    @Value("${app.jwt.refresh-token-expiration-ms}")
    private long refreshExpirationMs;

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw BusinessException.conflict("Email deja utilise : " + req.getEmail());
        }
        User user = User.builder()
                .email(req.getEmail())
                .firstName(req.getFirstName())
                .lastName(req.getLastName())
                .password(passwordEncoder.encode(req.getPassword()))
                .role(req.getRole())
                .build();
        userRepository.save(user);
        log.info("Nouvel utilisateur : {} [{}]", user.getEmail(), user.getRole());
        return buildAuthResponse(user);
    }

    @Transactional
    public AuthResponse login(LoginRequest req) {
        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword()));
        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> BusinessException.notFound("Utilisateur introuvable"));
        refreshTokenRepository.deleteByUser(user);
        log.info("Connexion : {}", user.getEmail());
        return buildAuthResponse(user);
    }

    @Transactional
    public AuthResponse refresh(String rawToken) {
        RefreshToken stored = refreshTokenRepository.findByToken(rawToken)
                .orElseThrow(() -> BusinessException.badRequest("Refresh token introuvable"));
        if (stored.isRevoked()) {
            refreshTokenRepository.deleteByUser(stored.getUser());
            log.warn("Replay token revoque pour {}", stored.getUser().getEmail());
            throw BusinessException.badRequest("Refresh token revoque");
        }
        if (stored.getExpiresAt().isBefore(Instant.now())) {
            throw BusinessException.badRequest("Refresh token expire");
        }
        stored.setRevoked(true);
        refreshTokenRepository.save(stored);
        return buildAuthResponse(stored.getUser());
    }

    private AuthResponse buildAuthResponse(User user) {
        Map<String, Object> claims = Map.of(
                "role",      user.getRole().name(),
                "userId",    user.getId().toString(),
                "firstName", user.getFirstName()
        );
        String accessToken  = jwtService.generateAccessToken(user.getEmail(), claims);
        String refreshToken = UUID.randomUUID().toString();
        refreshTokenRepository.save(RefreshToken.builder()
                .token(refreshToken)
                .user(user)
                .expiresAt(Instant.now().plusMillis(refreshExpirationMs))
                .build());
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(accessExpirationMs / 1000)
                .user(toUserResponse(user))
                .build();
    }

    private UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole().name())
                .createdAt(user.getCreatedAt())
                .build();
    }
}