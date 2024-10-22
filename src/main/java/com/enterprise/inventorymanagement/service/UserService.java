package com.enterprise.inventorymanagement.service;

import com.enterprise.inventorymanagement.exceptions.ResourceNotFoundException;
import com.enterprise.inventorymanagement.model.RoleName;
import com.enterprise.inventorymanagement.model.User;
import com.enterprise.inventorymanagement.model.dto.UserDTO;
import com.enterprise.inventorymanagement.model.request.UserRegistrationRequest;

import java.util.List;
import java.util.Optional;

public interface UserService {
    void registerUser(UserRegistrationRequest request);

    UserDTO getUserById(Long userId) throws ResourceNotFoundException;

    UserDTO updateUser(Long userId, UserDTO userDTO) throws ResourceNotFoundException;

    void assignRole(Long userId, RoleName roleName) throws ResourceNotFoundException;

    User saveUser(User user);
    Optional<User> getUserByUsername(String username);
    List<User> getAllUsers();
    User updateUser(Long id, User user);
    void deleteUser(Long id);
    void deactivateUser(Long id);
}

