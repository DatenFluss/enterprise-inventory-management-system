package com.enterprise.inventorymanagement.service;

import com.enterprise.inventorymanagement.model.User;
import com.enterprise.inventorymanagement.model.Role;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serial;
import java.util.Collection;
import java.util.stream.Collectors;

@Getter
public class UserDetailsImpl implements UserDetails {

    @Serial
    private static final long serialVersionUID = 1L;

    private final Long id;
    private final String fullName;
    private final String username;
    private final String email;
    private final String password;
    private final Long enterpriseId;
    private final Long managerId;
    private final String role;
    private final boolean active;
    private final Collection<? extends GrantedAuthority> authorities;

    public UserDetailsImpl(
            Long id,
            String fullName,
            String username,
            String email,
            String password,
            Long enterpriseId,
            Long managerId,
            String role,
            boolean active,
            Collection<? extends GrantedAuthority> authorities
    ) {
        this.id = id;
        this.fullName = fullName;
        this.username = username;
        this.email = email;
        this.password = password;
        this.enterpriseId = enterpriseId;
        this.managerId = managerId;
        this.role = role;
        this.active = active;
        this.authorities = authorities;
    }

    public static UserDetailsImpl build(User user) {
        Role role = user.getRole();
        Collection<GrantedAuthority> authorities = role.getPermissions().stream()
                .map(permission -> new SimpleGrantedAuthority(permission.getName()))
                .collect(Collectors.toList());

        // Add role itself as an authority
        authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));

        return new UserDetailsImpl(
                user.getId(),
                user.getFullName(),
                user.getUsername(),
                user.getEmail(),
                user.getPassword(),
                user.getEnterprise() != null ? user.getEnterprise().getId() : null,
                user.getManager() != null ? user.getManager().getId() : null,
                role.getName(),
                user.getActive(),
                authorities
        );
    }

    public boolean hasEnterpriseAccess(Long enterpriseId) {
        return this.enterpriseId != null && this.enterpriseId.equals(enterpriseId);
    }

    public boolean isManager() {
        return authorities.stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_MANAGER"));
    }

    public boolean isAdmin() {
        return authorities.stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    public boolean isOwner() {
        return authorities.stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_OWNER"));
    }

    public boolean hasManagerAccess(Long userId) {
        if (isAdmin() || isOwner()) return true;
        // Add logic to check if the user is a subordinate
        return isManager() && userId != null;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return active;
    }

    @Override
    public boolean isAccountNonLocked() {
        return active;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return active;
    }

    @Override
    public boolean isEnabled() {
        return active;
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
                ", role='" + role + '\'' +
                ", active=" + active +
                '}';
    }
}