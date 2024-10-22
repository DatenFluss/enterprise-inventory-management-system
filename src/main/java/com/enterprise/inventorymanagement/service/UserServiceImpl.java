package com.enterprise.inventorymanagement.service;

import com.enterprise.inventorymanagement.model.Role;
import com.enterprise.inventorymanagement.model.RoleName;
import com.enterprise.inventorymanagement.model.User;
import com.enterprise.inventorymanagement.model.dto.UserDTO;
import com.enterprise.inventorymanagement.model.request.UserRegistrationRequest;
import com.enterprise.inventorymanagement.repository.EnterpriseRepository;
import com.enterprise.inventorymanagement.repository.InventoryItemRepository;
import com.enterprise.inventorymanagement.repository.RoleRepository;
import com.enterprise.inventorymanagement.repository.UserRepository;
import com.enterprise.inventorymanagement.exceptions.ResourceNotFoundException;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

@Service("userService")
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
    @Transactional
    public void registerUser(UserRegistrationRequest request) {
        // Input validation
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username is already taken.");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email is already in use.");
        }

        // Create new user
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setActive(true);

        // Assign default role (e.g., EMPLOYEE)
        Role role = roleRepository.findByName(RoleName.EMPLOYEE.label)
                .orElseThrow(() -> new IllegalArgumentException("Default role not found."));
        user.setRole(role);

        userRepository.save(user);
    }

    @Override
    @Transactional
    public UserDTO getUserById(Long userId) throws ResourceNotFoundException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        return convertToDTO(user);
    }


    @Override
    @Transactional
    public UserDTO updateUser(Long userId, UserDTO userDTO) throws ResourceNotFoundException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        // Update fields
        user.setEmail(userDTO.getEmail());
        user.setActive(userDTO.isActive());

        userRepository.save(user);
        return convertToDTO(user);
    }

    @Override
    @Transactional
    public void assignRole(Long userId, RoleName roleName) throws ResourceNotFoundException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        Role role = roleRepository.findByName(roleName.label)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleName));

        user.setRole(role);
        userRepository.save(user);
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
    @PreAuthorize("hasAuthority('DEACTIVATE_USER') or (hasAuthority('DEACTIVATE_NON_PRIVILEGED_USER') and !@userService.isPrivilegedUser(#targetUserId))")
    public void deactivateUser(Long targetUserId) {
        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        targetUser.setActive(false);
        userRepository.save(targetUser);
    }

    public boolean isPrivilegedUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        String roleName = user.getRole().getName().toString();
        return roleName.equals("ENTERPRISE_MANAGER") || roleName.equals("OWNER") || roleName.equals("ADMIN");
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
        Role managerRole = roleRepository.findByName(RoleName.ENTERPRISE_MANAGER.label)
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

    // Helper method to convert User to UserDTO
    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setActive(user.getActive());
        dto.setRoleName(user.getRole().getName());
        return dto;
    }
}

