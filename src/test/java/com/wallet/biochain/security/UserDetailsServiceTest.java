package com.wallet.biochain.security;

import com.wallet.biochain.entities.User;
import com.wallet.biochain.enums.Role;
import com.wallet.biochain.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserDetailsService userDetailsService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setPassword("password123");
        user.setEmail("test@example.com");
        user.setFullName("Test User");
        user.setIsActive(true);
        user.setRoles(Set.of(Role.USER));
    }

    @Test
    void testLoadUserByUsername_UserExists_ReturnsUserDetails() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        // When
        UserDetails userDetails = userDetailsService.loadUserByUsername("testuser");

        // Then
        assertNotNull(userDetails);
        assertEquals("testuser", userDetails.getUsername());
        assertEquals("password123", userDetails.getPassword());
        assertTrue(userDetails.isEnabled());
        assertFalse(userDetails.getAuthorities().isEmpty());
        verify(userRepository, times(1)).findByUsername("testuser");
    }

    @Test
    void testLoadUserByUsername_UserNotFound_ThrowsException() {
        // Given
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // When & Then
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
            userDetailsService.loadUserByUsername("nonexistent");
        });

        assertEquals("User not found with username: nonexistent", exception.getMessage());
        verify(userRepository, times(1)).findByUsername("nonexistent");
    }

    @Test
    void testLoadUserByUsername_NullUsername_ThrowsException() {
        // Given
        when(userRepository.findByUsername(null)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(UsernameNotFoundException.class, () -> {
            userDetailsService.loadUserByUsername(null);
        });

        verify(userRepository, times(1)).findByUsername(null);
    }

    @Test
    void testLoadUserByUsername_EmptyUsername_ThrowsException() {
        // Given
        when(userRepository.findByUsername("")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(UsernameNotFoundException.class, () -> {
            userDetailsService.loadUserByUsername("");
        });

        verify(userRepository, times(1)).findByUsername("");
    }

    @Test
    void testLoadUserByUsername_InactiveUser_ReturnsDisabledUserDetails() {
        // Given
        user.setIsActive(false);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        // When
        UserDetails userDetails = userDetailsService.loadUserByUsername("testuser");

        // Then
        assertNotNull(userDetails);
        assertFalse(userDetails.isEnabled());
        verify(userRepository, times(1)).findByUsername("testuser");
    }

    @Test
    void testLoadUserByUsername_UserWithMultipleRoles_ReturnsCorrectAuthorities() {
        // Given
        user.setRoles(Set.of(Role.USER, Role.ADMIN));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        // When
        UserDetails userDetails = userDetailsService.loadUserByUsername("testuser");

        // Then
        assertNotNull(userDetails);
        assertEquals(2, userDetails.getAuthorities().size());
        verify(userRepository, times(1)).findByUsername("testuser");
    }

    @Test
    void testLoadUserByUsername_CaseInsensitiveUsername() {
        // Given
        when(userRepository.findByUsername("TestUser")).thenReturn(Optional.of(user));

        // When
        UserDetails userDetails = userDetailsService.loadUserByUsername("TestUser");

        // Then
        assertNotNull(userDetails);
        assertEquals("testuser", userDetails.getUsername());
        verify(userRepository, times(1)).findByUsername("TestUser");
    }

    @Test
    void testLoadUserByUsername_EmailAsUsername_Success() {
        // Given
        user.setUsername("test@example.com");
        when(userRepository.findByUsername("test@example.com")).thenReturn(Optional.of(user));

        // When
        UserDetails userDetails = userDetailsService.loadUserByUsername("test@example.com");

        // Then
        assertNotNull(userDetails);
        assertEquals("test@example.com", userDetails.getUsername());
        verify(userRepository, times(1)).findByUsername("test@example.com");
    }

    @Test
    void testLoadUserByUsername_SpecialCharactersInUsername_Success() {
        // Given
        user.setUsername("user+test@example.com");
        when(userRepository.findByUsername("user+test@example.com")).thenReturn(Optional.of(user));

        // When
        UserDetails userDetails = userDetailsService.loadUserByUsername("user+test@example.com");

        // Then
        assertNotNull(userDetails);
        assertEquals("user+test@example.com", userDetails.getUsername());
        verify(userRepository, times(1)).findByUsername("user+test@example.com");
    }

    @Test
    void testLoadUserByUsername_UserWithNoRoles_ReturnsEmptyAuthorities() {
        // Given
        user.setRoles(Set.of());
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        // When
        UserDetails userDetails = userDetailsService.loadUserByUsername("testuser");

        // Then
        assertNotNull(userDetails);
        assertTrue(userDetails.getAuthorities().isEmpty());
        verify(userRepository, times(1)).findByUsername("testuser");
    }

    @Test
    void testLoadUserByUsername_RepositoryCalledOnce() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        // When
        userDetailsService.loadUserByUsername("testuser");

        // Then
        verify(userRepository, times(1)).findByUsername("testuser");
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void testLoadUserByUsername_ReturnsUserDetailsImpl() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        // When
        UserDetails userDetails = userDetailsService.loadUserByUsername("testuser");

        // Then
        assertTrue(userDetails instanceof UserDetailsImpl);
        UserDetailsImpl userDetailsImpl = (UserDetailsImpl) userDetails;
        assertEquals(user.getId(), userDetailsImpl.getId());
        assertEquals(user.getEmail(), userDetailsImpl.getEmail());
    }
}

