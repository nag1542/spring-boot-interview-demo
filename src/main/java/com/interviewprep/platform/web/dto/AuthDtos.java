package com.interviewprep.platform.web.dto;

import jakarta.validation.constraints.*;

public class AuthDtos {
    public record RegisterRequest(
            @Email String email,
            @Size(min = 8) String password,
            @NotBlank String fullName,
            Integer roleId) {}
    public record LoginRequest(@Email String email, @NotBlank String password) {}
    public record RefreshRequest(@NotBlank String refreshToken) {}
    public record AuthResponse(String accessToken, String refreshToken) {}
}
