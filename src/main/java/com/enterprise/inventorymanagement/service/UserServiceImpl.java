package com.enterprise.inventorymanagement.service;

import com.enterprise.inventorymanagement.model.*;
import com.enterprise.inventorymanagement.model.dto.EnterpriseInviteDTO;
import com.enterprise.inventorymanagement.model.dto.UserDTO;
import com.enterprise.inventorymanagement.model.request.UserRegistrationRequest;
import com.enterprise.inventorymanagement.repository.*;
import com.enterprise.inventorymanagement.exceptions.ResourceNotFoundException;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service("userService")
@Slf4j
public class UserServiceImpl extends ServiceCommon implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    private final InviteRepository inviteRepository;

    @Autowired
    public UserServiceImpl(
            UserRepository userRepository,
            InventoryItemRepository itemRepository,
            RoleRepository roleRepository,
            EnterpriseRepository enterpriseRepository,
            InviteRepository inviteRepository,
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
        this.inviteRepository = inviteRepository;
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
        user.setFullName(request.getFullName());
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setActive(true);

        // Assign default unaffiliated role
        Role role = roleRepository.findByName(RoleName.UNAFFILIATED.name())
                .orElseThrow(() -> new IllegalArgumentException("Default role not found."));
        user.setRole(role);

        userRepository.save(user);
        log.info("User registered successfully: {}", request.getUsername());
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
        user.setActive(userDTO.getActive());

        // Handle role update if provided
        if (userDTO.getRoleName() != null) {
            Role role = roleRepository.findByName(userDTO.getRoleName())
                    .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + userDTO.getRoleName()));
            user.setRole(role);
        }

        // Handle enterprise update if provided
        if (userDTO.getEnterpriseId() != null) {
            Enterprise enterprise = enterpriseRepository.findById(userDTO.getEnterpriseId())
                    .orElseThrow(() -> new ResourceNotFoundException("Enterprise not found"));
            user.setEnterprise(enterprise);
        }

        // Handle manager update if provided
        if (userDTO.getManagerId() != null) {
            User manager = userRepository.findById(userDTO.getManagerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Manager not found"));
            user.setManager(manager);
        }

        userRepository.save(user);
        return convertToDTO(user);
    }

    @Override
    @Transactional
    public void assignRole(Long userId, RoleName roleName) throws ResourceNotFoundException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        Role role = roleRepository.findByName(roleName.name())
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleName));

        user.setRole(role);
        userRepository.save(user);
        log.info("Assigned role {} to user {}", roleName, userId);
    }

    @Override
    @Transactional
    public User saveUser(User user) {
        if (user.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        return userRepository.save(user);
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
    @Transactional
    public User updateUser(Long id, User updatedUser) {
        return userRepository.findById(id)
                .map(user -> {
                    user.setUsername(updatedUser.getUsername());
                    user.setEmail(updatedUser.getEmail());
                    if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
                        user.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
                    }
                    if (updatedUser.getRole() != null) {
                        user.setRole(updatedUser.getRole());
                    }
                    if (updatedUser.getEnterprise() != null) {
                        user.setEnterprise(updatedUser.getEnterprise());
                    }
                    if (updatedUser.getManager() != null) {
                        user.setManager(updatedUser.getManager());
                    }
                    return userRepository.save(user);
                })
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + id));
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found with id " + id);
        }

        // Before deleting, handle subordinates
        User user = userRepository.findById(id).get();
        if (user.getSubordinates() != null && !user.getSubordinates().isEmpty()) {
            user.getSubordinates().forEach(subordinate -> subordinate.setManager(null));
        }

        userRepository.deleteById(id);
        log.info("Deleted user with id: {}", id);
    }

    @Override
    @PreAuthorize("hasAuthority('DEACTIVATE_USER') or (hasAuthority('DEACTIVATE_NON_PRIVILEGED_USER') and !@userService.isPrivilegedUser(#id))")
    @Transactional
    public void deactivateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + id));

        user.setActive(false);
        userRepository.save(user);
        log.info("Deactivated user with id: {}", id);
    }

    public boolean isPrivilegedUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        String roleName = user.getRole().getName();
        return roleName.equals("MANAGER") || roleName.equals("OWNER") || roleName.equals("ADMIN");
    }

    @Override
    public UserDTO getCurrentUser() {
        User currentUser = getCurrentAuthenticatedUser();
        return convertToDTO(currentUser);
    }

    @Override
    public UserDTO getUserManager(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getManagerId() == null) {
            return null;
        }

        User manager = userRepository.findById(user.getManagerId())
                .orElseThrow(() -> new ResourceNotFoundException("Manager not found"));

        return convertToDTO(manager);
    }

    @Override
    public List<UserDTO> getSubordinates(Long managerId) {
        User manager = userRepository.findById(managerId)
                .orElseThrow(() -> new ResourceNotFoundException("Manager not found"));

        // Option 1: Using the relationship-based method
        return userRepository.findByManagerId(manager.getManagerId())
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    //@Override
    //public List<EnterpriseInviteDTO> getUserInvites(Long userId) {
    //    return inviteRepository.findByUserId(userId)
    //            .stream()
    //            .map(this::convertToInviteDTO)
    //            .collect(Collectors.toList());
    //}

    @Override
    @Transactional
    public void handleInviteResponse(Long inviteId, Long userId, boolean accepted) {
        EnterpriseInvite invite = inviteRepository.findById(inviteId)
                .orElseThrow(() -> new ResourceNotFoundException("Invite not found"));

        //if (!invite.getUserId().equals(userId)) {
        //    throw new AccessDeniedException("Not authorized to handle this invite");
        //}

        if (accepted) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            Enterprise enterprise = enterpriseRepository.findById(invite.getEnterpriseId())
                    .orElseThrow(() -> new ResourceNotFoundException("Enterprise not found"));

            user.setEnterprise(enterprise);
            user.setRole(roleRepository.findByName(invite.getRole().name())
                    .orElseThrow(() -> new ResourceNotFoundException("Role not found")));

            userRepository.save(user);
        }

        inviteRepository.delete(invite);
        log.info("User {} {} invite to enterprise {}",
                userId, accepted ? "accepted" : "declined", invite.getEnterpriseId());
    }

    @Override
    @Transactional
    public void assignManager(Long userId, Long managerId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (managerId == null) {
            user.setManager(null);
        } else {
            User manager = userRepository.findById(managerId)
                    .orElseThrow(() -> new ResourceNotFoundException("Manager not found"));

            // Validate that manager is in the same enterprise
            if (user.getEnterprise() == null || manager.getEnterprise() == null ||
                    !user.getEnterprise().getId().equals(manager.getEnterprise().getId())) {
                throw new IllegalArgumentException("Manager must be in the same enterprise");
            }

            user.setManager(manager);
        }

        userRepository.save(user);
    }

    @Override
    public boolean isUserInEnterprise(Long userId, Long enterpriseId) {
        return userRepository.findById(userId)
                .map(user -> user.getEnterprise() != null &&
                        user.getEnterprise().getId().equals(enterpriseId))
                .orElse(false);
    }

    @Override
    public List<UserDTO> getUsersByEnterprise(Long enterpriseId) {
        return userRepository.findByEnterpriseId(enterpriseId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void assignEnterprise(Long userId, Long enterpriseId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (enterpriseId == null) {
            user.setEnterprise(null);
        } else {
            Enterprise enterprise = enterpriseRepository.findById(enterpriseId)
                    .orElseThrow(() -> new ResourceNotFoundException("Enterprise not found"));
            user.setEnterprise(enterprise);
        }

        userRepository.save(user);
    }

    private EnterpriseInviteDTO convertToInviteDTO(EnterpriseInvite invite) {
        Enterprise enterprise = enterpriseRepository.findById(invite.getEnterpriseId())
                .orElseThrow(() -> new ResourceNotFoundException("Enterprise not found"));

        User inviter = userRepository.findById(invite.getInviterId())
                .orElseThrow(() -> new ResourceNotFoundException("Inviter not found"));

        return EnterpriseInviteDTO.builder()
                .id(invite.getId())
                .enterpriseName(enterprise.getName())
                .role(invite.getRole().toString())
                .inviterName(inviter.getUsername())
                .createdAt(invite.getCreatedAt())
                .build();
    }

    private UserDTO convertToDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .username(user.getUsername())
                .email(user.getEmail())
                .active(user.getActive())
                .enterpriseId(user.getEnterprise() != null ? user.getEnterprise().getId() : null)
                .managerId(user.getManager() != null ? user.getManager().getId() : null)
                .roleName(user.getRole().getName())
                .build();
    }
}
