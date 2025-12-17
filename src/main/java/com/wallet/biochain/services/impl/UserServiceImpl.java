package com.wallet.biochain.services.impl;

import com.wallet.biochain.dto.UserDTO;
import com.wallet.biochain.entities.User;
import com.wallet.biochain.enums.Role;
import com.wallet.biochain.mappers.UserMapper;
import com.wallet.biochain.repositories.UserRepository;
import com.wallet.biochain.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public UserDTO createUser(String username, String email, String fullName, String phoneNumber) {
        log.info("Creating user with username: {}", username);

        // Validate uniqueness
        if (usernameExists(username)) {
            throw new IllegalArgumentException("Username already exists: " + username);
        }

        if (emailExists(email)) {
            throw new IllegalArgumentException("Email already exists: " + email);
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setFullName(fullName);
        user.setPhoneNumber(phoneNumber);
        user.setIsActive(true);

        User savedUser = userRepository.save(user);
        log.info("User created successfully with ID: {}", savedUser.getId());

        return userMapper.toDTO(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserDTO> getUserById(Long id) {
        log.debug("Fetching user by ID: {}", id);
        return userRepository.findById(id)
                .map(userMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserDTO> getUserByUsername(String username) {
        log.debug("Fetching user by username: {}", username);
        return userRepository.findByUsername(username)
                .map(userMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserDTO> getUserByEmail(String email) {
        log.debug("Fetching user by email: {}", email);
        return userRepository.findByEmail(email)
                .map(userMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsers() {
        log.debug("Fetching all users");
        return userMapper.toDTOList(userRepository.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDTO> getActiveUsers() {
        log.debug("Fetching active users");
        return userMapper.toDTOList(userRepository.findByIsActive(true));
    }

    @Override
    @Transactional
    public UserDTO updateUser(Long id, String fullName, String phoneNumber) {
        log.info("Updating user with ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + id));

        if (fullName != null && !fullName.trim().isEmpty()) {
            user.setFullName(fullName);
        }

        if (phoneNumber != null && !phoneNumber.trim().isEmpty()) {
            user.setPhoneNumber(phoneNumber);
        }

        User updatedUser = userRepository.save(user);
        log.info("User updated successfully: {}", id);

        return userMapper.toDTO(updatedUser);
    }

    @Override
    @Transactional
    public void deactivateUser(Long id) {
        log.info("Deactivating user with ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + id));

        user.setIsActive(false);
        userRepository.save(user);

        log.info("User deactivated successfully: {}", id);
    }

    @Override
    @Transactional
    public void activateUser(Long id) {
        log.info("Activating user with ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + id));

        user.setIsActive(true);
        userRepository.save(user);

        log.info("User activated successfully: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDTO> searchUsers(String searchTerm) {
        log.debug("Searching users with term: {}", searchTerm);
        return userMapper.toDTOList(userRepository.searchUsers(searchTerm));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean usernameExists(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public Long countActiveUsers() {
        return userRepository.countActiveUsers();
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        log.info("Attempting to delete user with ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + id));

        // Check if user has active wallets
        long walletCount = user.getWallets() != null ? user.getWallets().stream()
                .filter(w -> Boolean.TRUE.equals(w.getIsActive()))
                .count() : 0;

        if (walletCount > 0) {
            throw new IllegalStateException("Cannot delete user with active wallets. Deactivate wallets first.");
        }

        userRepository.delete(user);
        log.info("User deleted successfully: {}", id);
    }

    @Override
    @Transactional
    public void assignRole(Long userId, Role role) {
        log.info("Assigning role {} to user {}", role, userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
        if (role == null) {
            throw new IllegalArgumentException("Role must not be null");
        }
        if (!user.getRoles().contains(role)) {
            user.getRoles().add(role);
            userRepository.save(user);
        } else {
            log.debug("User {} already has role {}", userId, role);
        }
    }

    @Override
    @Transactional
    public void removeRole(Long userId, Role role) {
        log.info("Removing role {} from user {}", role, userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
        if (role == null) {
            throw new IllegalArgumentException("Role must not be null");
        }
        if (user.getRoles().contains(role)) {
            user.getRoles().remove(role);
            userRepository.save(user);
        } else {
            log.debug("User {} does not have role {}", userId, role);
        }
    }
}