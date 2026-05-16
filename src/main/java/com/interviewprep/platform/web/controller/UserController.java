package com.interviewprep.platform.web.controller;

import com.interviewprep.platform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController @RequiredArgsConstructor
public class UserController {
    private final UserRepository userRepository;
    @GetMapping("/api/users/profile")
    public Object profile(Authentication authentication){ return userRepository.findByEmail(authentication.getName()).orElseThrow(); }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/api/admin/users")
    public Object allUsers(){ return userRepository.findAll(); }
}
