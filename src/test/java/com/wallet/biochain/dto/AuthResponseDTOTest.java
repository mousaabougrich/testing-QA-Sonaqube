package com.wallet.biochain.dto;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class AuthResponseDTOTest {

    @Test
    void testAuthResponseDTO_AllFields_Success() {
        // Given
        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...";
        String type = "Bearer";
        Long id = 1L;
        String username = "testuser";
        String email = "test@example.com";
        String fullName = "Test User";
        Set<String> roles = Set.of("ROLE_USER", "ROLE_ADMIN");

        // When
        AuthResponseDTO response = new AuthResponseDTO(token, type, id, username, email, fullName, roles);

        // Then
        assertEquals(token, response.token());
        assertEquals(type, response.type());
        assertEquals(id, response.id());
        assertEquals(username, response.username());
        assertEquals(email, response.email());
        assertEquals(fullName, response.fullName());
        assertEquals(roles, response.roles());
        assertEquals(2, response.roles().size());
    }

    @Test
    void testAuthResponseDTO_SingleRole_Success() {
        // Given
        Set<String> roles = Set.of("ROLE_USER");

        // When
        AuthResponseDTO response = new AuthResponseDTO(
                "token123",
                "Bearer",
                1L,
                "testuser",
                "test@example.com",
                "Test User",
                roles
        );

        // Then
        assertEquals(1, response.roles().size());
        assertTrue(response.roles().contains("ROLE_USER"));
    }

    @Test
    void testAuthResponseDTO_EmptyRoles_Success() {
        // Given
        Set<String> roles = Set.of();

        // When
        AuthResponseDTO response = new AuthResponseDTO(
                "token123",
                "Bearer",
                1L,
                "testuser",
                "test@example.com",
                "Test User",
                roles
        );

        // Then
        assertNotNull(response.roles());
        assertTrue(response.roles().isEmpty());
    }

    @Test
    void testAuthResponseDTO_NullFullName_Success() {
        // Given
        AuthResponseDTO response = new AuthResponseDTO(
                "token123",
                "Bearer",
                1L,
                "testuser",
                "test@example.com",
                null,
                Set.of("ROLE_USER")
        );

        // Then
        assertNull(response.fullName());
        assertNotNull(response.username());
        assertNotNull(response.email());
    }

    @Test
    void testRecordEquality_SameValues_ReturnsTrue() {
        // Given
        Set<String> roles = Set.of("ROLE_USER");
        AuthResponseDTO response1 = new AuthResponseDTO(
                "token123",
                "Bearer",
                1L,
                "testuser",
                "test@example.com",
                "Test User",
                roles
        );
        AuthResponseDTO response2 = new AuthResponseDTO(
                "token123",
                "Bearer",
                1L,
                "testuser",
                "test@example.com",
                "Test User",
                roles
        );

        // Then
        assertEquals(response1, response2);
        assertEquals(response1.hashCode(), response2.hashCode());
    }

    @Test
    void testRecordEquality_DifferentTokens_ReturnsFalse() {
        // Given
        AuthResponseDTO response1 = new AuthResponseDTO(
                "token123",
                "Bearer",
                1L,
                "testuser",
                "test@example.com",
                "Test User",
                Set.of("ROLE_USER")
        );
        AuthResponseDTO response2 = new AuthResponseDTO(
                "token456",
                "Bearer",
                1L,
                "testuser",
                "test@example.com",
                "Test User",
                Set.of("ROLE_USER")
        );

        // Then
        assertNotEquals(response1, response2);
    }

    @Test
    void testRecordEquality_DifferentIds_ReturnsFalse() {
        // Given
        AuthResponseDTO response1 = new AuthResponseDTO(
                "token123",
                "Bearer",
                1L,
                "testuser",
                "test@example.com",
                "Test User",
                Set.of("ROLE_USER")
        );
        AuthResponseDTO response2 = new AuthResponseDTO(
                "token123",
                "Bearer",
                2L,
                "testuser",
                "test@example.com",
                "Test User",
                Set.of("ROLE_USER")
        );

        // Then
        assertNotEquals(response1, response2);
    }

    @Test
    void testToString_ContainsFields() {
        // Given
        AuthResponseDTO response = new AuthResponseDTO(
                "token123",
                "Bearer",
                1L,
                "testuser",
                "test@example.com",
                "Test User",
                Set.of("ROLE_USER")
        );

        // When
        String toString = response.toString();

        // Then
        assertTrue(toString.contains("testuser"));
        assertTrue(toString.contains("test@example.com"));
        assertTrue(toString.contains("Bearer"));
    }

    @Test
    void testAuthResponseDTO_LongToken_Success() {
        // Given
        String longToken = "a".repeat(500);

        // When
        AuthResponseDTO response = new AuthResponseDTO(
                longToken,
                "Bearer",
                1L,
                "testuser",
                "test@example.com",
                "Test User",
                Set.of("ROLE_USER")
        );

        // Then
        assertEquals(longToken, response.token());
        assertEquals(500, response.token().length());
    }

    @Test
    void testAuthResponseDTO_MultipleRoles_Success() {
        // Given
        Set<String> roles = Set.of("ROLE_USER", "ROLE_ADMIN", "ROLE_MODERATOR");

        // When
        AuthResponseDTO response = new AuthResponseDTO(
                "token123",
                "Bearer",
                1L,
                "testuser",
                "test@example.com",
                "Test User",
                roles
        );

        // Then
        assertEquals(3, response.roles().size());
        assertTrue(response.roles().contains("ROLE_USER"));
        assertTrue(response.roles().contains("ROLE_ADMIN"));
        assertTrue(response.roles().contains("ROLE_MODERATOR"));
    }

    @Test
    void testAuthResponseDTO_DifferentTokenTypes_Success() {
        // Test different token types
        String[] tokenTypes = {"Bearer", "JWT", "Token", "OAuth"};

        for (String tokenType : tokenTypes) {
            AuthResponseDTO response = new AuthResponseDTO(
                    "token123",
                    tokenType,
                    1L,
                    "testuser",
                    "test@example.com",
                    "Test User",
                    Set.of("ROLE_USER")
            );

            assertEquals(tokenType, response.type());
        }
    }

    @Test
    void testAuthResponseDTO_NullToken_Success() {
        // Given & When
        AuthResponseDTO response = new AuthResponseDTO(
                null,
                "Bearer",
                1L,
                "testuser",
                "test@example.com",
                "Test User",
                Set.of("ROLE_USER")
        );

        // Then
        assertNull(response.token());
    }

    @Test
    void testAuthResponseDTO_SpecialCharactersInUsername_Success() {
        // Given
        AuthResponseDTO response = new AuthResponseDTO(
                "token123",
                "Bearer",
                1L,
                "user@example.com",
                "test@example.com",
                "Test User",
                Set.of("ROLE_USER")
        );

        // Then
        assertEquals("user@example.com", response.username());
    }
}

