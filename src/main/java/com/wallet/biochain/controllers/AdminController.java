package com.wallet.biochain.controllers;

import com.wallet.biochain.dto.UserDTO;
import com.wallet.biochain.enums.Role;
import com.wallet.biochain.security.UserDetailsImpl;
import com.wallet.biochain.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin", description = "Admin management endpoints")
public class AdminController {

    private final UserService userService;

    @GetMapping("/users")
    @Operation(summary = "Get all users", description = "Retrieves all users (Admin only)")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        log.info("Admin request to get all users");
        List<UserDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @PostMapping("/users/{userId}/roles")
    @Operation(summary = "Assign role to user", description = "Assigns a role to a user (Admin only)")
    public ResponseEntity<Void> assignRole(@PathVariable Long userId, @RequestParam Role role) {
        log.info("Admin request to assign role {} to user {}", role, userId);
        userService.assignRole(userId, role);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/users/{userId}/roles")
    @Operation(summary = "Remove role from user", description = "Removes a role from a user (Admin only)")
    public ResponseEntity<Void> removeRole(@PathVariable Long userId, @RequestParam Role role) {
        log.info("Admin request to remove role {} from user {}", role, userId);
        userService.removeRole(userId, role);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/me")
    @Operation(summary = "Get current admin info", description = "Returns current authenticated admin information")
    public ResponseEntity<UserDTO> getCurrentAdmin() {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        
        return userService.getUserById(userDetails.getId())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}

