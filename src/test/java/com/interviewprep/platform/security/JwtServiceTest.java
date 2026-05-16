package com.interviewprep.platform.security;

import com.interviewprep.platform.jwt.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JwtServiceTest {
    @Test
    void shouldGenerateAndParseToken() {
        JwtService jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", "1234567890123456789012345678901234567890");
        ReflectionTestUtils.setField(jwtService, "expiration", 3600000L);
        var user = org.springframework.security.core.userdetails.User.withUsername("test@a.com").password("x").authorities("ROLE_USER").build();
        String token = jwtService.generateToken(user);
        assertEquals("test@a.com", jwtService.extractUsername(token));
    }
}
