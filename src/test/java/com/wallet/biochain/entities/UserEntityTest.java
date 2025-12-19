package com.wallet.biochain.entities;

import com.wallet.biochain.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UserEntityTest {

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
    }

    @Test
    void testUserCreation() {
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("password123");
        user.setFullName("Test User");
        user.setPhoneNumber("+1234567890");
        user.setIsActive(true);

        assertEquals("testuser", user.getUsername());
        assertEquals("test@example.com", user.getEmail());
        assertEquals("password123", user.getPassword());
        assertEquals("Test User", user.getFullName());
        assertEquals("+1234567890", user.getPhoneNumber());
        assertTrue(user.getIsActive());
    }

    @Test
    void testUserRoles() {
        Set<Role> roles = new HashSet<>();
        roles.add(Role.USER);
        roles.add(Role.ADMIN);
        user.setRoles(roles);

        assertNotNull(user.getRoles());
        assertEquals(2, user.getRoles().size());
        assertTrue(user.getRoles().contains(Role.USER));
        assertTrue(user.getRoles().contains(Role.ADMIN));
    }

    @Test
    void testUserWallets() {
        user.setWallets(new ArrayList<>());
        Wallet wallet = new Wallet();
        wallet.setId(1L);
        user.getWallets().add(wallet);

        assertNotNull(user.getWallets());
        assertEquals(1, user.getWallets().size());
        assertEquals(1L, user.getWallets().get(0).getId());
    }

    @Test
    void testUserStakes() {
        user.setStakes(new ArrayList<>());
        Stake stake = new Stake();
        stake.setId(1L);
        user.getStakes().add(stake);

        assertNotNull(user.getStakes());
        assertEquals(1, user.getStakes().size());
    }

    @Test
    void testUserConstructorWithParams() {
        User newUser = new User("testuser", "test@example.com", "Test User");

        assertEquals("testuser", newUser.getUsername());
        assertEquals("test@example.com", newUser.getEmail());
        assertEquals("Test User", newUser.getFullName());
    }

    @Test
    void testOnCreate() {
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.onCreate();

        assertNotNull(user.getCreatedAt());
        assertNotNull(user.getUpdatedAt());
        assertTrue(user.getIsActive());
        assertNotNull(user.getRoles());
    }

    @Test
    void testOnUpdate() {
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.onCreate();

        LocalDateTime originalUpdatedAt = user.getUpdatedAt();
        user.onUpdate();

        assertNotNull(user.getUpdatedAt());
        assertTrue(user.getUpdatedAt().isAfter(originalUpdatedAt) || user.getUpdatedAt().isEqual(originalUpdatedAt));
    }
}

