package com.interviewprep.platform.web.controller;

import com.interviewprep.platform.repository.UserRepository;
import com.interviewprep.platform.web.dto.UserDtos;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class UserController {
    private final UserRepository userRepository;

    @GetMapping("/api/users/profile")
    public UserDtos.UserResponse profile(Authentication authentication) {
        return userRepository.findByEmailIgnoreCase(authentication.getName())
                .map(UserDtos.UserResponse::from)
                .orElseThrow();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/api/admin/users")
    public List<UserDtos.UserResponse> allUsers() {
        return userRepository.findAllWithRoles().stream().map(UserDtos.UserResponse::from).toList();
    }
}
