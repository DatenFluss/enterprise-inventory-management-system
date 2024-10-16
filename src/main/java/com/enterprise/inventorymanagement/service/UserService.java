package com.enterprise.inventorymanagement.service;

import com.enterprise.inventorymanagement.model.User;

import java.util.List;
import java.util.Optional;

public interface UserService {
    User saveUser(User user);
    Optional<User> getUserById(Long id);
    Optional<User> getUserByUsername(String username);
    List<User> getAllUsers();
    User updateUser(Long id, User user);
    void deleteUser(Long id);
    void deactivateUser(Long id);
}

