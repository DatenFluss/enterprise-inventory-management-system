package com.enterprise.inventorymanagement.security;

import com.enterprise.inventorymanagement.controller.UserController;
import com.enterprise.inventorymanagement.service.UserDetailsImpl;
import com.enterprise.inventorymanagement.service.UserDetailsServiceImpl;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Claims;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.StringUtils;
import org.springframework.security.authentication.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.HashSet;
import java.util.Set;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String jwt = getJwtFromRequest(request);
            log.debug("Processing request for path: {}", request.getServletPath());
            log.debug("JWT token present: {}", jwt != null);

            if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
                Claims claims = tokenProvider.getClaimsFromJWT(jwt);
                log.debug("Token validation successful for user ID: {}", claims.getSubject());

                // Extract role and authorities
                String role = claims.get("role", String.class);
                @SuppressWarnings("unchecked")
                List<String> authorities = claims.get("authorities", List.class);
                
                log.debug("Token role: {}", role);
                log.debug("Token authorities: {}", authorities);

                // Create granted authorities set
                Set<GrantedAuthority> grantedAuthorities = new HashSet<>();
                
                // Add role as authority
                if (role != null) {
                    grantedAuthorities.add(new SimpleGrantedAuthority(role));
                    log.debug("Added role authority: {}", role);
                }

                // Add additional authorities
                if (authorities != null) {
                    authorities.stream()
                            .map(SimpleGrantedAuthority::new)
                            .forEach(authority -> {
                                grantedAuthorities.add(authority);
                                log.debug("Added additional authority: {}", authority.getAuthority());
                            });
                }

                // Create UserDetailsImpl from claims
                UserDetailsImpl userDetails = new UserDetailsImpl(
                    Long.parseLong(claims.getSubject()),
                    claims.get("username", String.class),
                    claims.get("fullName", String.class),
                    claims.get("email", String.class),
                    "", // password not needed for token auth
                    claims.get("enterpriseId", Long.class),
                    role,
                    grantedAuthorities
                );

                // Create authentication token
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    grantedAuthorities
                );

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
                
                log.debug("Authentication set in SecurityContext for user: {}", userDetails.getUsername());
                log.debug("Authorities: {}", authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList()));
            }
        } catch (Exception ex) {
            log.error("Cannot set user authentication: {}", ex.getMessage());
            log.error("Stack trace:", ex);
        }

        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
