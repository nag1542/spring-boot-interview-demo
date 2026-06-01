package com.interviewprep.platform.service;

import com.interviewprep.platform.domain.RefreshToken;
import com.interviewprep.platform.domain.Role;
import com.interviewprep.platform.domain.User;
import com.interviewprep.platform.jwt.JwtService;
import com.interviewprep.platform.repository.RefreshTokenRepository;
import com.interviewprep.platform.repository.UserRepository;
import com.interviewprep.platform.web.dto.AuthDtos;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${app.refresh-token.expiration}")
    private long refreshTokenExpiration;

    @Transactional
    public AuthDtos.AuthResponse register(AuthDtos.RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email is already registered");
        }
        Role role = resolveRole(request.roleId());
        User saved = userRepository
                .save(User.builder().email(request.email()).password(passwordEncoder.encode(request.password()))
                        .fullName(request.fullName()).roles(Set.of(role)).build());
        String access = jwtService.generateToken(org.springframework.security.core.userdetails.User
                .withUsername(saved.getEmail()).password(saved.getPassword())
                .authorities(saved.getRoles().stream().map(Enum::name).toArray(String[]::new)).build());
        return new AuthDtos.AuthResponse(access, createRefreshToken(saved));
    }

    @Transactional
    public AuthDtos.AuthResponse login(AuthDtos.LoginRequest request) {
        authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        var u = userRepository.findByEmailIgnoreCase(request.email()).orElseThrow();
        String access = jwtService.generateToken(
                org.springframework.security.core.userdetails.User.withUsername(u.getEmail()).password(u.getPassword())
                        .authorities(u.getRoles().stream().map(Enum::name).toArray(String[]::new)).build());
        return new AuthDtos.AuthResponse(access, createRefreshToken(u));
    }

    @Transactional
    public AuthDtos.AuthResponse refresh(AuthDtos.RefreshRequest request) {
        RefreshToken refreshToken = refreshTokenRepository.findByTokenWithUserAndRoles(request.refreshToken())
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));
        if (refreshToken.isRevoked() || refreshToken.getExpiresAt().isBefore(Instant.now())) {
            throw new IllegalArgumentException("Refresh token is expired or revoked");
        }
        refreshToken.setRevoked(true);
        User user = refreshToken.getUser();
        String access = jwtService.generateToken(org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail()).password(user.getPassword())
                .authorities(user.getRoles().stream().map(Enum::name).toArray(String[]::new)).build());
        return new AuthDtos.AuthResponse(access, createRefreshToken(user));
    }

    private Role resolveRole(Integer roleId) {
        if (roleId == null || roleId == 1) {
            return Role.ROLE_USER;
        }
        if (roleId == 2) {
            return Role.ROLE_ADMIN;
        }
        if (roleId == 3) {
            return Role.ROLE_MANAGER;
        }
        throw new IllegalArgumentException("Invalid roleId. Use 1=ROLE_USER, 2=ROLE_ADMIN, 3=ROLE_MANAGER");
    }

    private String createRefreshToken(User user) {
        RefreshToken refreshToken = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .user(user)
                .expiresAt(Instant.now().plusMillis(refreshTokenExpiration))
                .revoked(false)
                .build();
        return refreshTokenRepository.save(refreshToken).getToken();
    }
}
