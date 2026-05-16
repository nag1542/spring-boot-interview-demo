package com.interviewprep.platform.web.controller;

import com.interviewprep.platform.service.AuthService;
import com.interviewprep.platform.web.dto.AuthDtos;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/auth") @RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    @PostMapping("/register") public ResponseEntity<AuthDtos.AuthResponse> register(@Valid @RequestBody AuthDtos.RegisterRequest req){ return ResponseEntity.ok(authService.register(req)); }
    @PostMapping("/login") public ResponseEntity<AuthDtos.AuthResponse> login(@Valid @RequestBody AuthDtos.LoginRequest req){ return ResponseEntity.ok(authService.login(req)); }
    @PostMapping("/refresh") public ResponseEntity<AuthDtos.AuthResponse> refresh(){ return ResponseEntity.ok(new AuthDtos.AuthResponse("new-access","new-refresh")); }
}
