package com.interviewprep.platform.web.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class DemoController {

    @GetMapping("/api/demo/public")
    public Map<String, String> publicAccess() {
        return Map.of(
                "message", "Public endpoint: no JWT required",
                "access", "anonymous");
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/api/demo/admin")
    public Map<String, String> adminAccess(Authentication authentication) {
        return Map.of(
                "message", "Admin endpoint: valid JWT with ROLE_ADMIN required",
                "user", authentication.getName());
    }
}
