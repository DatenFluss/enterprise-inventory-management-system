package com.enterprise.inventorymanagement.service;

import com.enterprise.inventorymanagement.model.User;
import com.enterprise.inventorymanagement.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import java.util.stream.Collectors;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(UserDetailsServiceImpl.class);

    private final UserRepository userRepository;

    @Autowired
    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User Not Found with username: " + username));

        if (!Boolean.TRUE.equals(user.getActive())) {
            throw new UsernameNotFoundException("User is disabled");
        }

        // Force loading of permissions within transaction
        int permissionCount = user.getRole().getPermissions().size();
        logger.debug("Loaded {} permissions for user {}", permissionCount, username);
        logger.debug("Role: {}", user.getRole().getName());
        logger.debug("Permissions: {}", user.getRole().getPermissions().stream()
                .map(p -> p.getName())
                .collect(Collectors.toList()));

        return UserDetailsImpl.build(user);
    }

    @Transactional
    public UserDetails loadUserById(Long id) throws UsernameNotFoundException {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with id: " + id));

        if (!Boolean.TRUE.equals(user.getActive())) {
            throw new UsernameNotFoundException("User is disabled");
        }

        // Force loading of permissions within transaction
        int permissionCount = user.getRole().getPermissions().size();
        logger.debug("Loaded {} permissions for user {}", permissionCount, user.getUsername());
        logger.debug("Role: {}", user.getRole().getName());
        logger.debug("Permissions: {}", user.getRole().getPermissions().stream()
                .map(p -> p.getName())
                .collect(Collectors.toList()));

        // Create UserDetails with loaded permissions
        UserDetails userDetails = UserDetailsImpl.build(user);
        logger.debug("Built UserDetails with authorities: {}", userDetails.getAuthorities());
        return userDetails;
    }
}
