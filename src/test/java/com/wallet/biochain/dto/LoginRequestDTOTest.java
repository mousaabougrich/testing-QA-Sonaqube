package com.wallet.biochain.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class LoginRequestDTOTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testValidLoginRequest_Success() {
        // Given
        LoginRequestDTO loginRequest = new LoginRequestDTO("testuser", "password123");

        // When
        Set<ConstraintViolation<LoginRequestDTO>> violations = validator.validate(loginRequest);

        // Then
        assertTrue(violations.isEmpty());
        assertEquals("testuser", loginRequest.username());
        assertEquals("password123", loginRequest.password());
    }

    @ParameterizedTest
    @CsvSource({
        "'', password123, Username is required",
        ", password123, Username is required",
        "testuser, '', Password is required",
        "testuser, , Password is required"
    })
    void testInvalidFields_ValidationFails(String username, String password, String expectedMessage) {
        // Given
        LoginRequestDTO loginRequest = new LoginRequestDTO(username, password);

        // When
        Set<ConstraintViolation<LoginRequestDTO>> violations = validator.validate(loginRequest);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().equals(expectedMessage)));
    }

    @Test
    void testBothFieldsBlank_ValidationFails() {
        // Given
        LoginRequestDTO loginRequest = new LoginRequestDTO("", "");

        // When
        Set<ConstraintViolation<LoginRequestDTO>> violations = validator.validate(loginRequest);

        // Then
        assertFalse(violations.isEmpty());
        assertEquals(2, violations.size());
    }

    @Test
    void testWhitespaceUsername_ValidationFails() {
        // Given
        LoginRequestDTO loginRequest = new LoginRequestDTO("   ", "password123");

        // When
        Set<ConstraintViolation<LoginRequestDTO>> violations = validator.validate(loginRequest);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().equals("Username is required")));
    }

    @Test
    void testWhitespacePassword_ValidationFails() {
        // Given
        LoginRequestDTO loginRequest = new LoginRequestDTO("testuser", "   ");

        // When
        Set<ConstraintViolation<LoginRequestDTO>> violations = validator.validate(loginRequest);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().equals("Password is required")));
    }

    @Test
    void testLongUsernameAndPassword_Success() {
        // Given
        String longUsername = "a".repeat(100);
        String longPassword = "b".repeat(100);
        LoginRequestDTO loginRequest = new LoginRequestDTO(longUsername, longPassword);

        // When
        Set<ConstraintViolation<LoginRequestDTO>> violations = validator.validate(loginRequest);

        // Then
        assertTrue(violations.isEmpty());
        assertEquals(longUsername, loginRequest.username());
        assertEquals(longPassword, loginRequest.password());
    }

    @Test
    void testSpecialCharactersInUsername_Success() {
        // Given
        LoginRequestDTO loginRequest = new LoginRequestDTO("user@example.com", "Pass@123!");

        // When
        Set<ConstraintViolation<LoginRequestDTO>> violations = validator.validate(loginRequest);

        // Then
        assertTrue(violations.isEmpty());
    }

    @Test
    void testRecordEquality_SameValues_ReturnsTrue() {
        // Given
        LoginRequestDTO request1 = new LoginRequestDTO("testuser", "password123");
        LoginRequestDTO request2 = new LoginRequestDTO("testuser", "password123");

        // Then
        assertEquals(request1, request2);
        assertEquals(request1.hashCode(), request2.hashCode());
    }

    @Test
    void testRecordEquality_DifferentValues_ReturnsFalse() {
        // Given
        LoginRequestDTO request1 = new LoginRequestDTO("testuser", "password123");
        LoginRequestDTO request2 = new LoginRequestDTO("otheruser", "password456");

        // Then
        assertNotEquals(request1, request2);
    }

    @Test
    void testToString_ContainsFields() {
        // Given
        LoginRequestDTO loginRequest = new LoginRequestDTO("testuser", "password123");

        // When
        String toString = loginRequest.toString();

        // Then
        assertTrue(toString.contains("testuser"));
        assertTrue(toString.contains("password123"));
    }
}

