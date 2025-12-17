package com.wallet.biochain.services;

import com.wallet.biochain.dto.UserDTO;
import com.wallet.biochain.enums.Role;

import java.util.List;
import java.util.Optional;

public interface UserService {

    /**
     * Create a new user
     */
    UserDTO createUser(String username, String email, String fullName, String phoneNumber);

    /**
     * Get user by ID
     */
    Optional<UserDTO> getUserById(Long id);

    /**
     * Get user by username
     */
    Optional<UserDTO> getUserByUsername(String username);

    /**
     * Get user by email
     */
    Optional<UserDTO> getUserByEmail(String email);

    /**
     * Get all users
     */
    List<UserDTO> getAllUsers();

    /**
     * Get active users only
     */
    List<UserDTO> getActiveUsers();

    /**
     * Update user information
     */
    UserDTO updateUser(Long id, String fullName, String phoneNumber);

    /**
     * Deactivate user account
     */
    void deactivateUser(Long id);

    /**
     * Activate user account
     */
    void activateUser(Long id);

    /**
     * Search users by username or email
     */
    List<UserDTO> searchUsers(String searchTerm);

    /**
     * Check if username exists
     */
    boolean usernameExists(String username);

    /**
     * Check if email exists
     */
    boolean emailExists(String email);

    /**
     * Get total active users count
     */
    Long countActiveUsers();

    /**
     * Delete user (only if no active wallets)
     */
    void deleteUser(Long id);

    /**
     * Assign a role to a user (admin service)
     */
    void assignRole(Long userId, Role role);

    /**
     * Remove a role from a user (admin service)
     */
    void removeRole(Long userId, Role role);
}