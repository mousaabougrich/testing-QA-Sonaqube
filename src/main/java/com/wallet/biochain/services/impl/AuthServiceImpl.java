package com.wallet.biochain.services.impl;

import com.wallet.biochain.dto.AuthResponseDTO;
import com.wallet.biochain.dto.LoginRequestDTO;
import com.wallet.biochain.dto.RegisterRequestDTO;
import com.wallet.biochain.entities.User;
import com.wallet.biochain.enums.Role;
import com.wallet.biochain.repositories.UserRepository;
import com.wallet.biochain.security.JwtUtil;
import com.wallet.biochain.security.UserDetailsImpl;
import com.wallet.biochain.services.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @Override
    @Transactional
    public AuthResponseDTO register(RegisterRequestDTO registerRequest) {
        log.info("Registering new user: {}", registerRequest.username());

        // Check if username or email already exists
        if (userRepository.existsByUsername(registerRequest.username())) {
            throw new IllegalArgumentException("Username already exists: " + registerRequest.username());
        }

        if (userRepository.existsByEmail(registerRequest.email())) {
            throw new IllegalArgumentException("Email already exists: " + registerRequest.email());
        }

        // Create new user
        User user = new User();
        user.setUsername(registerRequest.username());
        user.setEmail(registerRequest.email());
        user.setPassword(passwordEncoder.encode(registerRequest.password()));
        user.setFullName(registerRequest.fullName());
        user.setPhoneNumber(registerRequest.phoneNumber());
        user.setIsActive(true);

        // Set default role as USER
        Set<Role> roles = new HashSet<>();
        roles.add(Role.USER);
        user.setRoles(roles);

        User savedUser = userRepository.save(user);
        log.info("User registered successfully with ID: {}", savedUser.getId());

        // Generate JWT token
        UserDetailsImpl userDetails = new UserDetailsImpl(savedUser);
        String token = jwtUtil.generateToken(userDetails);

        return buildAuthResponse(token, savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponseDTO login(LoginRequestDTO loginRequest) {
        log.info("Attempting to login user: {}", loginRequest.username());

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.username(),
                            loginRequest.password()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            String token = jwtUtil.generateToken(userDetails);

            User user = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found after authentication"));

            log.info("User logged in successfully: {}", userDetails.getUsername());
            return buildAuthResponse(token, user);

        } catch (BadCredentialsException e) {
            log.error("Invalid credentials for user: {}", loginRequest.username());
            throw new IllegalArgumentException("Invalid username or password");
        }
    }

    private AuthResponseDTO buildAuthResponse(String token, User user) {
        return new AuthResponseDTO(
                token,
                "Bearer",
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.getRoles().stream().map(Role::name).collect(Collectors.toSet())
        );
    }
}

