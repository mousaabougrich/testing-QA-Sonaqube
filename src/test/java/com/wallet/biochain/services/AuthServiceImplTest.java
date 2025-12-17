package com.wallet.biochain.services;

import com.wallet.biochain.dto.AuthResponseDTO;
import com.wallet.biochain.dto.LoginRequestDTO;
import com.wallet.biochain.dto.RegisterRequestDTO;
import com.wallet.biochain.entities.User;
import com.wallet.biochain.enums.Role;
import com.wallet.biochain.repositories.UserRepository;
import com.wallet.biochain.security.JwtUtil;
import com.wallet.biochain.security.UserDetailsImpl;
import com.wallet.biochain.services.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthServiceImpl authService;

    private User user;

    @BeforeEach
    void setup() {
        user = new User();
        user.setId(1L);
        user.setUsername("john");
        user.setEmail("john@example.com");
        user.setPassword("encoded");
        user.setFullName("John Doe");
        user.setIsActive(true);
        Set<Role> roles = new HashSet<>();
        roles.add(Role.USER);
        user.setRoles(roles);
    }

    @Test
    void register_success() {
        RegisterRequestDTO req = new RegisterRequestDTO("john", "john@example.com", "pwd", "John Doe", "+212600000000");

        when(userRepository.existsByUsername("john")).thenReturn(false);
        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(passwordEncoder.encode("pwd")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });
        when(jwtUtil.generateToken(any(UserDetailsImpl.class))).thenReturn("jwt-token");

        AuthResponseDTO res = authService.register(req);

        assertNotNull(res);
        assertEquals("jwt-token", res.token());
        assertEquals("Bearer", res.type());
        assertEquals("john", res.username());
        assertTrue(res.roles().contains("USER"));
    }

    @Test
    void login_success() {
        LoginRequestDTO req = new LoginRequestDTO("john", "pwd");

        Authentication auth = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);
        when(auth.getPrincipal()).thenReturn(new UserDetailsImpl(user));
        when(jwtUtil.generateToken(any(UserDetailsImpl.class))).thenReturn("jwt-token");
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));

        AuthResponseDTO res = authService.login(req);
        assertEquals("jwt-token", res.token());
        assertEquals(1L, res.id());
        assertEquals("john", res.username());
    }

    @Test
    void login_badCredentials_throws() {
        LoginRequestDTO req = new LoginRequestDTO("john", "bad");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("bad"));

        assertThrows(IllegalArgumentException.class, () -> authService.login(req));
    }
}
