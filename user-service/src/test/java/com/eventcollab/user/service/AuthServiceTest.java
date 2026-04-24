package com.eventcollab.user.service;

import com.eventcollab.common.exception.BusinessException;
import com.eventcollab.user.domain.*;
import com.eventcollab.user.dto.*;
import com.eventcollab.user.repository.*;
import com.eventcollab.user.security.JwtService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock UserRepository         userRepository;
    @Mock RefreshTokenRepository refreshTokenRepository;
    @Mock PasswordEncoder        passwordEncoder;
    @Mock JwtService             jwtService;
    @Mock AuthenticationManager  authManager;
    @InjectMocks AuthService     authService;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(authService, "accessExpirationMs",  900_000L);
        ReflectionTestUtils.setField(authService, "refreshExpirationMs", 604_800_000L);
    }

    @Test
    @DisplayName("register : succes avec email unique")
    void register_success() {
        RegisterRequest req = buildRegisterRequest("alice@test.com");
        when(userRepository.existsByEmail("alice@test.com")).thenReturn(false);
        when(passwordEncoder.encode("password8")).thenReturn("hashed");
        when(jwtService.generateAccessToken(any(), any())).thenReturn("jwt.access.token");
        when(userRepository.save(any())).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            ReflectionTestUtils.setField(u, "id", UUID.randomUUID());
            return u;
        });
        AuthResponse result = authService.register(req);
        assertThat(result.getAccessToken()).isEqualTo("jwt.access.token");
        assertThat(result.getRefreshToken()).isNotBlank();
        verify(userRepository).save(any(User.class));
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("register : leve CONFLICT si email deja utilise")
    void register_emailConflict() {
        RegisterRequest req = buildRegisterRequest("bob@test.com");
        when(userRepository.existsByEmail("bob@test.com")).thenReturn(true);
        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Email deja utilise");
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("login : succes avec bonnes credentials")
    void login_success() {
        User user = buildUser("carol@test.com");
        LoginRequest req = new LoginRequest();
        req.setEmail("carol@test.com");
        req.setPassword("password8");
        when(userRepository.findByEmail("carol@test.com")).thenReturn(Optional.of(user));
        when(jwtService.generateAccessToken(any(), any())).thenReturn("jwt.access.token");
        AuthResponse result = authService.login(req);
        assertThat(result.getAccessToken()).isEqualTo("jwt.access.token");
        verify(authManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(refreshTokenRepository).deleteByUser(user);
    }

    @Test
    @DisplayName("login : leve exception si bad credentials")
    void login_badCredentials() {
        LoginRequest req = new LoginRequest();
        req.setEmail("bad@test.com");
        req.setPassword("wrong");
        doThrow(new BadCredentialsException("Bad credentials")).when(authManager).authenticate(any());
        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(BadCredentialsException.class);
        verify(userRepository, never()).findByEmail(any());
    }

    @Test
    @DisplayName("refresh : rotation succes")
    void refresh_success() {
        User user = buildUser("dave@test.com");
        RefreshToken stored = RefreshToken.builder()
                .token("valid-token").user(user)
                .expiresAt(Instant.now().plusSeconds(3600)).revoked(false).build();
        when(refreshTokenRepository.findByToken("valid-token")).thenReturn(Optional.of(stored));
        when(jwtService.generateAccessToken(any(), any())).thenReturn("new.access.token");
        AuthResponse result = authService.refresh("valid-token");
        assertThat(result.getAccessToken()).isEqualTo("new.access.token");
        assertThat(stored.isRevoked()).isTrue();
    }

    @Test
    @DisplayName("refresh : leve exception si token revoque")
    void refresh_revoked() {
        User user = buildUser("eve@test.com");
        RefreshToken stored = RefreshToken.builder()
                .token("revoked-token").user(user)
                .expiresAt(Instant.now().plusSeconds(3600)).revoked(true).build();
        when(refreshTokenRepository.findByToken("revoked-token")).thenReturn(Optional.of(stored));
        assertThatThrownBy(() -> authService.refresh("revoked-token"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("revoque");
        verify(refreshTokenRepository).deleteByUser(user);
    }

    @Test
    @DisplayName("refresh : leve exception si token expire")
    void refresh_expired() {
        RefreshToken stored = RefreshToken.builder()
                .token("expired-token").user(buildUser("f@test.com"))
                .expiresAt(Instant.now().minusSeconds(60)).revoked(false).build();
        when(refreshTokenRepository.findByToken("expired-token")).thenReturn(Optional.of(stored));
        assertThatThrownBy(() -> authService.refresh("expired-token"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("expire");
    }

    private RegisterRequest buildRegisterRequest(String email) {
        RegisterRequest req = new RegisterRequest();
        req.setEmail(email); req.setFirstName("Test");
        req.setLastName("User"); req.setPassword("password8");
        return req;
    }

    private User buildUser(String email) {
        return User.builder().id(UUID.randomUUID()).email(email)
                .firstName("Test").lastName("User")
                .password("hashed").role(Role.USER).enabled(true).build();
    }
}