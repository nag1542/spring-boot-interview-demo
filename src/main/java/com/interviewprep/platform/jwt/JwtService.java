package com.interviewprep.platform.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JwtService {
    @Value("${app.jwt.secret}")
    private String secret;
    @Value("${app.jwt.expiration}")
    private long expiration;

    public String generateToken(UserDetails user) {
        return Jwts.builder().subject(user.getUsername()).issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration)).signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractUsername(String token) {
        return parse(token).getSubject();
    }

    public boolean isValid(String token, UserDetails user) {
        return extractUsername(token).equals(user.getUsername()) && parse(token).getExpiration().after(new Date());
    }

    private Claims parse(String token) {
        return Jwts.parser().verifyWith(key()).build().parseSignedClaims(token).getPayload();
    }

    private SecretKey key() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }
}
