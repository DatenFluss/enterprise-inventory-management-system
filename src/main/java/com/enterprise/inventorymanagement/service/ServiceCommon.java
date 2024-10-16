package com.enterprise.inventorymanagement.service;

import com.enterprise.inventorymanagement.model.User;
import com.enterprise.inventorymanagement.repository.EnterpriseRepository;
import com.enterprise.inventorymanagement.repository.InventoryItemRepository;
import com.enterprise.inventorymanagement.repository.RoleRepository;
import com.enterprise.inventorymanagement.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class ServiceCommon {

    protected final UserRepository userRepository;
    protected final InventoryItemRepository itemRepository;
    protected final RoleRepository roleRepository;
    protected final EnterpriseRepository enterpriseRepository;
    protected final PasswordEncoder passwordEncoder;
    protected final AuthenticationFacade authenticationFacade;

    @Autowired
    public ServiceCommon(
            UserRepository userRepository,
            InventoryItemRepository itemRepository,
            RoleRepository roleRepository,
            EnterpriseRepository enterpriseRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationFacade authenticationFacade
    ) {
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
        this.roleRepository = roleRepository;
        this.enterpriseRepository = enterpriseRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationFacade = authenticationFacade;
    }

    /**
     * Get the current authenticated user.
     */
    protected User getCurrentAuthenticatedUser() {
        String username = authenticationFacade.getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    /**
     * Check if the user has a specific permission.
     */
    protected boolean hasPermission(User user, String permissionName) {
        return user.getPermissions().stream()
                .anyMatch(permission -> permission.getName().equals(permissionName));
    }
}
