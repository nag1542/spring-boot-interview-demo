package com.interviewprep.platform.service;

import com.interviewprep.platform.domain.Role;
import com.interviewprep.platform.domain.User;
import com.interviewprep.platform.jwt.JwtService;
import com.interviewprep.platform.repository.UserRepository;
import com.interviewprep.platform.web.dto.AuthDtos;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service @RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository; private final PasswordEncoder passwordEncoder; private final JwtService jwtService; private final AuthenticationManager authenticationManager;
    public AuthDtos.AuthResponse register(AuthDtos.RegisterRequest request) {
        User saved = userRepository.save(User.builder().email(request.email()).password(passwordEncoder.encode(request.password())).fullName(request.fullName()).roles(Set.of(Role.ROLE_USER)).build());
        String access = jwtService.generateToken(org.springframework.security.core.userdetails.User.withUsername(saved.getEmail()).password(saved.getPassword()).authorities(saved.getRoles().stream().map(Enum::name).toArray(String[]::new)).build());
        return new AuthDtos.AuthResponse(access, "refresh-token-placeholder");
    }
    public AuthDtos.AuthResponse login(AuthDtos.LoginRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        var u = userRepository.findByEmail(request.email()).orElseThrow();
        String access = jwtService.generateToken(org.springframework.security.core.userdetails.User.withUsername(u.getEmail()).password(u.getPassword()).authorities(u.getRoles().stream().map(Enum::name).toArray(String[]::new)).build());
        return new AuthDtos.AuthResponse(access, "refresh-token-placeholder");
    }
}
