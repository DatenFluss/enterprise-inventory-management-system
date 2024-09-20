package com.enterprise.inventorymanagemet.service;

import com.enterprise.inventorymanagemet.model.Role;
import com.enterprise.inventorymanagemet.model.RoleName;
import com.enterprise.inventorymanagemet.model.User;
import com.enterprise.inventorymanagemet.repository.EnterpriseRepository;
import com.enterprise.inventorymanagemet.repository.InventoryItemRepository;
import com.enterprise.inventorymanagemet.repository.RoleRepository;
import com.enterprise.inventorymanagemet.repository.UserRepository;
import com.enterprise.inventorymanagemet.exceptions.ResourceNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl extends ServiceCommon implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    public UserServiceImpl(
            UserRepository userRepository,
            InventoryItemRepository itemRepository,
            RoleRepository roleRepository,
            EnterpriseRepository enterpriseRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationFacade authenticationFacade
    ) {
        super(
                userRepository,
                itemRepository,
                roleRepository,
                enterpriseRepository,
                passwordEncoder,
                authenticationFacade
        );
    }

    @Override
    public User saveUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    @Override
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User updateUser(Long id, User updatedUser) {
        return userRepository.findById(id)
                .map(user -> {
                    user.setUsername(updatedUser.getUsername());
                    user.setEmail(updatedUser.getEmail());
                    if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
                        user.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
                    }
                    user.setRole(updatedUser.getRole());
                    return userRepository.save(user);
                })
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + id));
    }

    /**
     * Deactivate a user.
     * Managers cannot deactivate other managers or owners.
     */
    public void deactivateUser(Long targetUserId) {
        User currentUser = getCurrentAuthenticatedUser();
        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Check if current user has DEACTIVATE_USER permission
        if (hasPermission(currentUser, "DEACTIVATE_USER")) {
            // User can deactivate any user
            targetUser.setIsActive(false);
            userRepository.save(targetUser);
            return;
        }

        // Check if current user has DEACTIVATE_NON_PRIVILEGED_USER permission
        if (hasPermission(currentUser, "DEACTIVATE_NON_PRIVILEGED_USER")) {
            // Ensure target user is not a manager or owner
            if (!isPrivilegedUser(targetUser)) {
                targetUser.setIsActive(false);
                userRepository.save(targetUser);
            } else {
                throw new AccessDeniedException("You do not have permission to deactivate this user.");
            }
            return;
        }

        throw new AccessDeniedException("You do not have permission to deactivate users.");
    }

    private boolean isPrivilegedUser(User user) {
        String roleName = String.valueOf(user.getRole().getName());
        return roleName.equals("MANAGER") || roleName.equals("OWNER") || roleName.equals("ADMIN");
    }

    /**
     * Promote a user to manager.
     * Only owners can promote users.
     */
    public void promoteUserToManager(Long userId) {
        User currentUser = getCurrentAuthenticatedUser();
        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Check if current user has PROMOTE_USER permission
        if (!hasPermission(currentUser, "PROMOTE_USER")) {
            throw new AccessDeniedException("You do not have permission to promote users.");
        }

        // Update the target user's role to MANAGER
        Role managerRole = roleRepository.findByName(RoleName.ENTERPRISE_MANAGER)
                .orElseThrow(() -> new IllegalStateException("MANAGER role not found"));

        targetUser.setRole(managerRole);
        userRepository.save(targetUser);
    }

    @Override
    public void deleteUser(Long id) {
        logger.warn("Deleting user with id: {}", id);
        if (!userRepository.existsById(id)) {
            logger.error("User not found with id: {}", id);
            throw new ResourceNotFoundException("User not found with id " + id);
        }
        userRepository.deleteById(id);
    }
}

