package com.enterprise.inventorymanagement.service;

import com.enterprise.inventorymanagement.model.User;
import com.enterprise.inventorymanagement.model.Role;
import com.enterprise.inventorymanagement.model.Permission;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serial;
import java.util.Collection;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;

@Getter
public class UserDetailsImpl implements UserDetails {

    private static final Logger logger = LoggerFactory.getLogger(UserDetailsImpl.class);

    @Serial
    private static final long serialVersionUID = 1L;

    private final Long id;
    private final String username;
    private final String fullName;
    private final String email;
    private final String password;
    private final Long enterpriseId;
    private final Long departmentId;
    private final String departmentName;
    private final String role;
    private final Collection<? extends GrantedAuthority> authorities;

    public UserDetailsImpl(Long id, String username, String fullName, String email, String password, 
                         Long enterpriseId, Long departmentId, String departmentName, String role, 
                         Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.username = username;
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.enterpriseId = enterpriseId;
        this.departmentId = departmentId;
        this.departmentName = departmentName;
        this.role = role;
        this.authorities = authorities;
        logger.debug("Created UserDetailsImpl - username: {}, role: {}, departmentId: {}, authorities: {}", 
            username, role, departmentId, authorities);
    }

    public static UserDetailsImpl build(User user) {
        logger.debug("Building UserDetails for user: {} with role: {}", 
            user.getUsername(), user.getRole().getName());
        
        // Get role name and ensure it has ROLE_ prefix
        String roleName = user.getRole().getName().toString();
        String roleAuthority = roleName.startsWith("ROLE_") ? roleName : "ROLE_" + roleName;
        
        Set<GrantedAuthority> authorities = new HashSet<>();
        authorities.add(new SimpleGrantedAuthority(roleAuthority));
        
        // Add permissions as authorities
        if (user.getRole().getPermissions() != null) {
            authorities.addAll(user.getRole().getPermissions().stream()
                    .map(permission -> new SimpleGrantedAuthority(permission.getName()))
                    .collect(Collectors.toSet()));
        }

        logger.debug("Role authority: {}", roleAuthority);
        logger.debug("All authorities: {}", authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));

        return new UserDetailsImpl(
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                user.getEmail(),
                user.getPassword(),
                user.getEnterprise() != null ? user.getEnterprise().getId() : null,
                user.getDepartment() != null ? user.getDepartment().getId() : null,
                user.getDepartment() != null ? user.getDepartment().getName() : null,
                roleAuthority,
                authorities
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserDetailsImpl user = (UserDetailsImpl) o;
        return id.equals(user.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "UserDetailsImpl{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", enterpriseId=" + enterpriseId +
                ", departmentId=" + departmentId +
                ", departmentName='" + departmentName + '\'' +
                ", role='" + role + '\'' +
                ", authorities=" + authorities.stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.joining(", ")) +
                '}';
    }
}