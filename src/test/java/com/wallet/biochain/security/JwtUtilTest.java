package com.wallet.biochain.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", "mySecretKeyForJWTTokenGenerationThatShouldBeAtLeast256BitsLong");
        ReflectionTestUtils.setField(jwtUtil, "expiration", 86400000L); // 24 hours

        userDetails = User.builder()
                .username("testuser")
                .password("password")
                .authorities(Collections.emptyList())
                .build();
    }

    @Test
    void testGenerateToken_Success() {
        // When
        String token = jwtUtil.generateToken(userDetails);

        // Then
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void testGenerateTokenWithExtraClaims_Success() {
        // Given
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("userId", 123L);
        extraClaims.put("email", "test@example.com");

        // When
        String token = jwtUtil.generateToken(userDetails, extraClaims);

        // Then
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void testExtractUsername_Success() {
        // Given
        String token = jwtUtil.generateToken(userDetails);

        // When
        String username = jwtUtil.extractUsername(token);

        // Then
        assertEquals("testuser", username);
    }

    @Test
    void testExtractExpiration_Success() {
        // Given
        String token = jwtUtil.generateToken(userDetails);

        // When
        Date expiration = jwtUtil.extractExpiration(token);

        // Then
        assertNotNull(expiration);
        assertTrue(expiration.after(new Date()));
    }

    @Test
    void testValidateToken_ValidToken_ReturnsTrue() {
        // Given
        String token = jwtUtil.generateToken(userDetails);

        // When
        Boolean isValid = jwtUtil.validateToken(token, userDetails);

        // Then
        assertTrue(isValid);
    }

    @Test
    void testValidateToken_WrongUsername_ReturnsFalse() {
        // Given
        String token = jwtUtil.generateToken(userDetails);
        UserDetails differentUser = User.builder()
                .username("differentuser")
                .password("password")
                .authorities(Collections.emptyList())
                .build();

        // When
        Boolean isValid = jwtUtil.validateToken(token, differentUser);

        // Then
        assertFalse(isValid);
    }

    @Test
    void testValidateToken_ExpiredToken_ReturnsFalse() {
        // Given
        ReflectionTestUtils.setField(jwtUtil, "expiration", -1000L); // Expired token
        String token = jwtUtil.generateToken(userDetails);
        ReflectionTestUtils.setField(jwtUtil, "expiration", 86400000L); // Reset

        // When & Then
        // ExpiredJwtException will be thrown when trying to validate an expired token
        assertThrows(ExpiredJwtException.class, () -> {
            jwtUtil.validateToken(token, userDetails);
        });
    }

    @Test
    void testExtractUsername_InvalidToken_ThrowsException() {
        // Given
        String invalidToken = "invalid.token.here";

        // When & Then
        assertThrows(MalformedJwtException.class, () -> {
            jwtUtil.extractUsername(invalidToken);
        });
    }

    @Test
    void testExtractUsername_NullToken_ThrowsException() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            jwtUtil.extractUsername(null);
        });
    }

    @Test
    void testExtractClaim_CustomClaim_Success() {
        // Given
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("customClaim", "customValue");
        String token = jwtUtil.generateToken(userDetails, extraClaims);

        // When
        String customClaim = jwtUtil.extractClaim(token, claims -> claims.get("customClaim", String.class));

        // Then
        assertEquals("customValue", customClaim);
    }

    @Test
    void testTokenNotExpired_FreshToken() {
        // Given
        String token = jwtUtil.generateToken(userDetails);

        // When
        Date expiration = jwtUtil.extractExpiration(token);

        // Then
        assertTrue(expiration.after(new Date()));
    }

    @Test
    void testGenerateToken_WithLongUsername_Success() {
        // Given
        UserDetails longUsernameUser = User.builder()
                .username("verylongusernamethatexceedsnormallength@example.com")
                .password("password")
                .authorities(Collections.emptyList())
                .build();

        // When
        String token = jwtUtil.generateToken(longUsernameUser);

        // Then
        assertNotNull(token);
        assertEquals("verylongusernamethatexceedsnormallength@example.com", jwtUtil.extractUsername(token));
    }

    @Test
    void testGenerateToken_WithSpecialCharactersInUsername_Success() {
        // Given
        UserDetails specialCharUser = User.builder()
                .username("user+test@example.com")
                .password("password")
                .authorities(Collections.emptyList())
                .build();

        // When
        String token = jwtUtil.generateToken(specialCharUser);

        // Then
        assertNotNull(token);
        assertEquals("user+test@example.com", jwtUtil.extractUsername(token));
    }
}

