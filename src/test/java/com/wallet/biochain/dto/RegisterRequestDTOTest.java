package com.wallet.biochain.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class RegisterRequestDTOTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testValidRegisterRequest_AllFields_Success() {
        // Given
        RegisterRequestDTO registerRequest = new RegisterRequestDTO(
                "testuser",
                "test@example.com",
                "password123",
                "Test User",
                "1234567890"
        );

        // When
        Set<ConstraintViolation<RegisterRequestDTO>> violations = validator.validate(registerRequest);

        // Then
        assertTrue(violations.isEmpty());
        assertEquals("testuser", registerRequest.username());
        assertEquals("test@example.com", registerRequest.email());
        assertEquals("password123", registerRequest.password());
        assertEquals("Test User", registerRequest.fullName());
        assertEquals("1234567890", registerRequest.phoneNumber());
    }

    @Test
    void testValidRegisterRequest_RequiredFieldsOnly_Success() {
        // Given
        RegisterRequestDTO registerRequest = new RegisterRequestDTO(
                "testuser",
                "test@example.com",
                "password123",
                null,
                null
        );

        // When
        Set<ConstraintViolation<RegisterRequestDTO>> violations = validator.validate(registerRequest);

        // Then
        assertTrue(violations.isEmpty());
    }

    @Test
    void testBlankUsername_ValidationFails() {
        // Given
        RegisterRequestDTO registerRequest = new RegisterRequestDTO(
                "",
                "test@example.com",
                "password123",
                "Test User",
                null
        );

        // When
        Set<ConstraintViolation<RegisterRequestDTO>> violations = validator.validate(registerRequest);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().equals("Username is required")));
    }

    @Test
    void testNullUsername_ValidationFails() {
        // Given
        RegisterRequestDTO registerRequest = new RegisterRequestDTO(
                null,
                "test@example.com",
                "password123",
                null,
                null
        );

        // When
        Set<ConstraintViolation<RegisterRequestDTO>> violations = validator.validate(registerRequest);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().equals("Username is required")));
    }

    @Test
    void testBlankEmail_ValidationFails() {
        // Given
        RegisterRequestDTO registerRequest = new RegisterRequestDTO(
                "testuser",
                "",
                "password123",
                null,
                null
        );

        // When
        Set<ConstraintViolation<RegisterRequestDTO>> violations = validator.validate(registerRequest);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().equals("Email is required")));
    }

    @Test
    void testNullEmail_ValidationFails() {
        // Given
        RegisterRequestDTO registerRequest = new RegisterRequestDTO(
                "testuser",
                null,
                "password123",
                null,
                null
        );

        // When
        Set<ConstraintViolation<RegisterRequestDTO>> violations = validator.validate(registerRequest);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().equals("Email is required")));
    }

    @Test
    void testInvalidEmailFormat_ValidationFails() {
        // Given
        RegisterRequestDTO registerRequest = new RegisterRequestDTO(
                "testuser",
                "invalid-email",
                "password123",
                null,
                null
        );

        // When
        Set<ConstraintViolation<RegisterRequestDTO>> violations = validator.validate(registerRequest);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().equals("Email should be valid")));
    }

    @Test
    void testBlankPassword_ValidationFails() {
        // Given
        RegisterRequestDTO registerRequest = new RegisterRequestDTO(
                "testuser",
                "test@example.com",
                "",
                null,
                null
        );

        // When
        Set<ConstraintViolation<RegisterRequestDTO>> violations = validator.validate(registerRequest);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().equals("Password is required")));
    }

    @Test
    void testNullPassword_ValidationFails() {
        // Given
        RegisterRequestDTO registerRequest = new RegisterRequestDTO(
                "testuser",
                "test@example.com",
                null,
                null,
                null
        );

        // When
        Set<ConstraintViolation<RegisterRequestDTO>> violations = validator.validate(registerRequest);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().equals("Password is required")));
    }

    @Test
    void testAllRequiredFieldsBlank_ValidationFails() {
        // Given
        RegisterRequestDTO registerRequest = new RegisterRequestDTO("", "", "", null, null);

        // When
        Set<ConstraintViolation<RegisterRequestDTO>> violations = validator.validate(registerRequest);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.size() >= 3);
    }

    @Test
    void testWhitespaceUsername_ValidationFails() {
        // Given
        RegisterRequestDTO registerRequest = new RegisterRequestDTO(
                "   ",
                "test@example.com",
                "password123",
                null,
                null
        );

        // When
        Set<ConstraintViolation<RegisterRequestDTO>> violations = validator.validate(registerRequest);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().equals("Username is required")));
    }

    @Test
    void testValidEmailFormats_Success() {
        // Test various valid email formats
        String[] validEmails = {
                "test@example.com",
                "user.name@example.com",
                "user+tag@example.co.uk",
                "test123@test-domain.com"
        };

        for (String email : validEmails) {
            RegisterRequestDTO registerRequest = new RegisterRequestDTO(
                    "testuser",
                    email,
                    "password123",
                    null,
                    null
            );

            Set<ConstraintViolation<RegisterRequestDTO>> violations = validator.validate(registerRequest);
            assertTrue(violations.isEmpty(), "Email should be valid: " + email);
        }
    }

    @Test
    void testInvalidEmailFormats_ValidationFails() {
        // Test various invalid email formats
        String[] invalidEmails = {
                "plaintext",
                "@example.com",
                "user@",
                "user @example.com",
                "user@.com"
        };

        for (String email : invalidEmails) {
            RegisterRequestDTO registerRequest = new RegisterRequestDTO(
                    "testuser",
                    email,
                    "password123",
                    null,
                    null
            );

            Set<ConstraintViolation<RegisterRequestDTO>> violations = validator.validate(registerRequest);
            assertFalse(violations.isEmpty(), "Email should be invalid: " + email);
        }
    }

    @Test
    void testLongFieldValues_Success() {
        // Given
        String longUsername = "a".repeat(100);
        String longPassword = "b".repeat(100);
        String longFullName = "c".repeat(100);
        RegisterRequestDTO registerRequest = new RegisterRequestDTO(
                longUsername,
                "test@example.com",
                longPassword,
                longFullName,
                "1234567890"
        );

        // When
        Set<ConstraintViolation<RegisterRequestDTO>> violations = validator.validate(registerRequest);

        // Then
        assertTrue(violations.isEmpty());
    }

    @Test
    void testRecordEquality_SameValues_ReturnsTrue() {
        // Given
        RegisterRequestDTO request1 = new RegisterRequestDTO(
                "testuser",
                "test@example.com",
                "password123",
                "Test User",
                "1234567890"
        );
        RegisterRequestDTO request2 = new RegisterRequestDTO(
                "testuser",
                "test@example.com",
                "password123",
                "Test User",
                "1234567890"
        );

        // Then
        assertEquals(request1, request2);
        assertEquals(request1.hashCode(), request2.hashCode());
    }

    @Test
    void testRecordEquality_DifferentValues_ReturnsFalse() {
        // Given
        RegisterRequestDTO request1 = new RegisterRequestDTO(
                "testuser",
                "test@example.com",
                "password123",
                null,
                null
        );
        RegisterRequestDTO request2 = new RegisterRequestDTO(
                "otheruser",
                "other@example.com",
                "password456",
                null,
                null
        );

        // Then
        assertNotEquals(request1, request2);
    }

    @Test
    void testToString_ContainsFields() {
        // Given
        RegisterRequestDTO registerRequest = new RegisterRequestDTO(
                "testuser",
                "test@example.com",
                "password123",
                "Test User",
                "1234567890"
        );

        // When
        String toString = registerRequest.toString();

        // Then
        assertTrue(toString.contains("testuser"));
        assertTrue(toString.contains("test@example.com"));
    }

    @Test
    void testPhoneNumberVariousFormats_Success() {
        // Test various phone number formats (optional field, no validation)
        String[] phoneNumbers = {
                "1234567890",
                "+1-234-567-8900",
                "(123) 456-7890",
                "+33 6 12 34 56 78",
                null
        };

        for (String phoneNumber : phoneNumbers) {
            RegisterRequestDTO registerRequest = new RegisterRequestDTO(
                    "testuser",
                    "test@example.com",
                    "password123",
                    "Test User",
                    phoneNumber
            );

            Set<ConstraintViolation<RegisterRequestDTO>> violations = validator.validate(registerRequest);
            assertTrue(violations.isEmpty(), "Phone number should be accepted: " + phoneNumber);
        }
    }
}

