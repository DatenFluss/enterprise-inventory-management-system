package com.enterprise.inventorymanagement.security;

import com.enterprise.inventorymanagement.service.UserDetailsServiceImpl;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .authorizeHttpRequests(authz -> {
                logger.debug("Configuring authorization rules");
                authz
                    .requestMatchers(HttpMethod.POST, "/api/users/register", "/api/users/login", "/api/enterprises/register").permitAll()
                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                    .requestMatchers("/api/users/me", "/api/enterprises/current").authenticated()
                    .requestMatchers("/api/users/subordinates/**").hasAuthority("VIEW_SUBORDINATES")
                    .requestMatchers("/api/enterprises/employees/**").hasAnyRole("MANAGER", "ENTERPRISE_OWNER", "ADMIN")
                    .requestMatchers("/api/enterprises/*/warehouses/**").hasAuthority("VIEW_WAREHOUSES")
                    .requestMatchers("/api/inventory/**", "/api/items/**").hasAuthority("VIEW_INVENTORY")
                    .requestMatchers("/api/departments/*/employees").hasAuthority("VIEW_INVENTORY")
                    .requestMatchers("/api/requests/my").hasAuthority("VIEW_MY_REQUESTS")
                    .requestMatchers("/api/requests/**").hasAnyAuthority("VIEW_REQUESTS", "MANAGE_REQUESTS", "VIEW_PENDING_REQUESTS", "VIEW_OWN_REQUESTS")
                    .requestMatchers("/api/department-invites/my").authenticated()
                    .requestMatchers("/api/department-invites/**").hasAnyAuthority("VIEW_DEPARTMENT_INVITES", "MANAGE_DEPARTMENT")
                    .anyRequest().authenticated();
                logger.debug("Authorization rules configured successfully");
            })
            .exceptionHandling(exceptionHandling -> exceptionHandling
                .authenticationEntryPoint((request, response, authException) -> {
                    logger.error("Unauthorized error: {}", authException.getMessage());
                    logger.error("Request URI: {}", request.getRequestURI());
                    logger.error("Request method: {}", request.getMethod());
                    logger.error("User authorities: {}", 
                        SecurityContextHolder.getContext().getAuthentication() != null ? 
                        SecurityContextHolder.getContext().getAuthentication().getAuthorities() : "No authentication");
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Error: Unauthorized");
                })
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Accept", "*"));
        configuration.setExposedHeaders(Arrays.asList("Authorization"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}


