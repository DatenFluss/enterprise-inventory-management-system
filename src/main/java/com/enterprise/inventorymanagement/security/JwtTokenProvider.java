package com.enterprise.inventorymanagement.security;

import com.enterprise.inventorymanagement.service.UserDetailsImpl;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;


import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenProvider {

    @Value("${app.jwtSecret}")
    private String jwtSecret;

    @Value("${app.jwtExpirationMs}")
    private int jwtExpirationMs;

    private Key key;

    @PostConstruct
    public void init() {
        key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    /**
     * Generate a JWT token based on authentication information.
     */
    public String generateToken(Authentication authentication) {
        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .subject(String.valueOf(userPrincipal.getId()))
                .claim("username", userPrincipal.getUsername())
                .claim("fullName", userPrincipal.getUsername())
                .claim("email", userPrincipal.getEmail())
                .claim("role", userPrincipal.getRole())
                .claim("enterpriseId", userPrincipal.getEnterpriseId())
                .claim("managerId", userPrincipal.getEnterpriseId())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }

    /**
     * Get authentication information from the token.
     */
    public Long getUserIdFromJWT(String token) {
        Claims claims = Jwts.parser()
                .verifyWith((SecretKey) key)
                .build().parseSignedClaims(token).getPayload();

        return Long.parseLong(claims.getSubject());
    }

    /**
     * Validate the JWT token.
     */
    public boolean validateToken(String authToken) {
        try {
            Jwts.parser()
                    .verifyWith((SecretKey) key)
                    .build()
                    .parseSignedClaims(authToken);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            // Invalid JWT token
            System.out.println("Invalid JWT token: " + e.getMessage());
        }
        return false;
    }
}
