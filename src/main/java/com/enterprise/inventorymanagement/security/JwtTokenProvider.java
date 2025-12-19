package com.enterprise.inventorymanagement.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import com.enterprise.inventorymanagement.service.UserDetailsImpl;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    @Value("${app.jwtSecret:${JWT_SECRET:}}")
    private String jwtSecret;

    @Value("${app.jwtExpirationMs:${JWT_EXPIRATION_MS:86400000}}")
    private int jwtExpirationMs;

    private SecretKey key;
    private JwtParser jwtParser;

    @PostConstruct
    public void init() {
        if (jwtSecret == null || jwtSecret.trim().isEmpty()) {
            throw new IllegalStateException("JWT secret is not configured");
        }

        byte[] secretBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        if (secretBytes.length < 64) {
            throw new IllegalStateException("JWT secret must be at least 64 bytes (512 bits) for HS512");
        }

        key = Keys.hmacShaKeyFor(secretBytes);
        jwtParser = Jwts.parserBuilder()
                .setSigningKey(key)
                .build();
    }

    public String generateToken(Authentication authentication) {
        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        final String roleWithPrefix = userPrincipal.getRole() != null && !userPrincipal.getRole().startsWith("ROLE_") 
            ? "ROLE_" + userPrincipal.getRole()
            : userPrincipal.getRole();

        List<String> authorities = userPrincipal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(auth -> !auth.equals(roleWithPrefix))
                .collect(Collectors.toList());

        logger.debug("Generating token for user: {}", userPrincipal.getUsername());
        logger.debug("Role: {}", roleWithPrefix);
        logger.debug("Additional authorities: {}", authorities);

        String token = Jwts.builder()
                .setSubject(String.valueOf(userPrincipal.getId()))
                .claim("username", userPrincipal.getUsername())
                .claim("fullName", userPrincipal.getFullName())
                .claim("email", userPrincipal.getEmail())
                .claim("enterpriseId", userPrincipal.getEnterpriseId())
                .claim("departmentId", userPrincipal.getDepartmentId())
                .claim("departmentName", userPrincipal.getDepartmentName())
                .claim("role", roleWithPrefix)
                .claim("authorities", authorities)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();

        logger.debug("Generated token claims - role: {}, authorities: {}, departmentId: {}", 
            roleWithPrefix, authorities, userPrincipal.getDepartmentId());
        return token;
    }

    public Long getUserIdFromJWT(String token) {
        Claims claims = jwtParser.parseClaimsJws(token).getBody();

        Long userId = Long.parseLong(claims.getSubject());
        logger.debug("Extracted user ID from token: {}", userId);
        return userId;
    }

    public String getUserRoleFromJWT(String token) {
        Claims claims = jwtParser.parseClaimsJws(token).getBody();

        String role = claims.get("role", String.class);
        logger.debug("Extracted role from token: {}", role);
        return role;
    }

    public Claims getClaimsFromJWT(String token) {
        Claims claims = jwtParser.parseClaimsJws(token).getBody();
        
        logger.debug("Retrieved claims from token:");
        logger.debug("Role: {}", claims.get("role"));
        logger.debug("Authorities: {}", claims.get("authorities", List.class));
        logger.debug("EnterpriseId: {}", claims.get("enterpriseId"));
        return claims;
    }

    public boolean validateToken(String authToken) {
        try {
            Claims claims = jwtParser.parseClaimsJws(authToken).getBody();

            if (claims.get("role") == null) {
                logger.error("Token validation failed: no role claim");
                return false;
            }
            
            logger.debug("Token validation successful");
            return true;
        } catch (SignatureException ex) {
            logger.error("Invalid JWT signature: {}", ex.getMessage());
        } catch (MalformedJwtException ex) {
            logger.error("Invalid JWT token: {}", ex.getMessage());
        } catch (ExpiredJwtException ex) {
            logger.error("Expired JWT token: {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            logger.error("Unsupported JWT token: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            logger.error("JWT claims string is empty: {}", ex.getMessage());
        }
        return false;
    }
}
