package com.wallet.biochain.controllers;

import com.wallet.biochain.dto.UserDTO;
import com.wallet.biochain.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User", description = "User management endpoints")
public class UserController {

    private final UserService userService;

    @PostMapping
    @Operation(summary = "Create user", description = "Creates a new user account")
    public ResponseEntity<UserDTO> createUser(
            @RequestParam String username,
            @RequestParam String email,
            @RequestParam String fullName,
            @RequestParam(required = false) String phoneNumber) {
        log.info("REST request to create user: {}", username);

        try {
            UserDTO user = userService.createUser(username, email, fullName, phoneNumber);
            return ResponseEntity.status(HttpStatus.CREATED).body(user);
        } catch (IllegalArgumentException e) {
            log.error("Failed to create user", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID", description = "Retrieves user by ID")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        log.info("REST request to get user: {}", id);

        return userService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/username/{username}")
    @Operation(summary = "Get user by username", description = "Retrieves user by username")
    public ResponseEntity<UserDTO> getUserByUsername(@PathVariable String username) {
        log.info("REST request to get user by username: {}", username);

        return userService.getUserByUsername(username)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/email/{email}")
    @Operation(summary = "Get user by email", description = "Retrieves user by email")
    public ResponseEntity<UserDTO> getUserByEmail(@PathVariable String email) {
        log.info("REST request to get user by email: {}", email);

        return userService.getUserByEmail(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(summary = "Get all users", description = "Retrieves all users")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        log.info("REST request to get all users");

        List<UserDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/active")
    @Operation(summary = "Get active users", description = "Retrieves only active users")
    public ResponseEntity<List<UserDTO>> getActiveUsers() {
        log.info("REST request to get active users");

        List<UserDTO> users = userService.getActiveUsers();
        return ResponseEntity.ok(users);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update user", description = "Updates user information")
    public ResponseEntity<UserDTO> updateUser(
            @PathVariable Long id,
            @RequestParam(required = false) String fullName,
            @RequestParam(required = false) String phoneNumber) {
        log.info("REST request to update user: {}", id);

        try {
            UserDTO user = userService.updateUser(id, fullName, phoneNumber);
            return ResponseEntity.ok(user);
        } catch (IllegalArgumentException e) {
            log.error("Failed to update user", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{id}/activate")
    @Operation(summary = "Activate user", description = "Activates a user account")
    public ResponseEntity<Void> activateUser(@PathVariable Long id) {
        log.info("REST request to activate user: {}", id);

        try {
            userService.activateUser(id);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate user", description = "Deactivates a user account")
    public ResponseEntity<Void> deactivateUser(@PathVariable Long id) {
        log.info("REST request to deactivate user: {}", id);

        try {
            userService.deactivateUser(id);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/search")
    @Operation(summary = "Search users", description = "Searches users by username or email")
    public ResponseEntity<List<UserDTO>> searchUsers(@RequestParam String searchTerm) {
        log.info("REST request to search users: {}", searchTerm);

        List<UserDTO> users = userService.searchUsers(searchTerm);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/exists/username/{username}")
    @Operation(summary = "Check username exists", description = "Checks if username exists")
    public ResponseEntity<Boolean> usernameExists(@PathVariable String username) {
        log.debug("REST request to check username exists: {}", username);

        boolean exists = userService.usernameExists(username);
        return ResponseEntity.ok(exists);
    }

    @GetMapping("/exists/email/{email}")
    @Operation(summary = "Check email exists", description = "Checks if email exists")
    public ResponseEntity<Boolean> emailExists(@PathVariable String email) {
        log.debug("REST request to check email exists: {}", email);

        boolean exists = userService.emailExists(email);
        return ResponseEntity.ok(exists);
    }

    @GetMapping("/count/active")
    @Operation(summary = "Count active users", description = "Gets count of active users")
    public ResponseEntity<Long> countActiveUsers() {
        log.info("REST request to count active users");

        Long count = userService.countActiveUsers();
        return ResponseEntity.ok(count);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete user", description = "Deletes a user (if no active wallets)")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        log.info("REST request to delete user: {}", id);

        try {
            userService.deleteUser(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalStateException | IllegalArgumentException e) {
            log.error("Failed to delete user", e);
            return ResponseEntity.badRequest().build();
        }
    }
}