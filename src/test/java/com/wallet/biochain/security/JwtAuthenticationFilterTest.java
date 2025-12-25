package com.wallet.biochain.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.IOException;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        userDetails = User.builder()
                .username("testuser")
                .password("password")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")))
                .build();
    }

    @Test
    void testDoFilterInternal_ValidToken_SetsAuthentication() throws ServletException, IOException {
        // Given
        String token = "validToken";
        String authHeader = "Bearer " + token;

        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(jwtUtil.extractUsername(token)).thenReturn("testuser");
        when(userDetailsService.loadUserByUsername("testuser")).thenReturn(userDetails);
        when(jwtUtil.validateToken(token, userDetails)).thenReturn(true);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("testuser", SecurityContextHolder.getContext().getAuthentication().getName());
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_NoAuthHeader_ContinuesFilterChain() throws ServletException, IOException {
        // Given
        when(request.getHeader("Authorization")).thenReturn(null);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain, times(1)).doFilter(request, response);
        verify(jwtUtil, never()).extractUsername(anyString());
    }

    @Test
    void testDoFilterInternal_InvalidAuthHeaderFormat_ContinuesFilterChain() throws ServletException, IOException {
        // Given
        when(request.getHeader("Authorization")).thenReturn("InvalidFormat token");

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain, times(1)).doFilter(request, response);
        verify(jwtUtil, never()).extractUsername(anyString());
    }

    @Test
    void testDoFilterInternal_InvalidToken_DoesNotSetAuthentication() throws ServletException, IOException {
        // Given
        String token = "invalidToken";
        String authHeader = "Bearer " + token;

        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(jwtUtil.extractUsername(token)).thenReturn("testuser");
        when(userDetailsService.loadUserByUsername("testuser")).thenReturn(userDetails);
        when(jwtUtil.validateToken(token, userDetails)).thenReturn(false);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_ExpiredToken_ContinuesFilterChain() throws ServletException, IOException {
        // Given
        String token = "expiredToken";
        String authHeader = "Bearer " + token;

        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(jwtUtil.extractUsername(token)).thenThrow(new ExpiredJwtException(null, null, "Token expired"));

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_MalformedToken_ContinuesFilterChain() throws ServletException, IOException {
        // Given
        String token = "malformedToken";
        String authHeader = "Bearer " + token;

        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(jwtUtil.extractUsername(token)).thenThrow(new MalformedJwtException("Malformed token"));

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_NullUsername_DoesNotSetAuthentication() throws ServletException, IOException {
        // Given
        String token = "tokenWithNullUsername";
        String authHeader = "Bearer " + token;

        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(jwtUtil.extractUsername(token)).thenReturn(null);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain, times(1)).doFilter(request, response);
        verify(userDetailsService, never()).loadUserByUsername(anyString());
    }

    @Test
    void testDoFilterInternal_EmptyAuthHeader_ContinuesFilterChain() throws ServletException, IOException {
        // Given
        when(request.getHeader("Authorization")).thenReturn("");

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_BearerOnlyNoToken_ContinuesFilterChain() throws ServletException, IOException {
        // Given
        when(request.getHeader("Authorization")).thenReturn("Bearer ");

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_ExistingAuthentication_SkipsTokenValidation() throws ServletException, IOException {
        // Given
        String token = "validToken";
        String authHeader = "Bearer " + token;

        // Set existing authentication
        SecurityContextHolder.getContext().setAuthentication(
            new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                "existingUser", null, Collections.emptyList()
            )
        );

        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(jwtUtil.extractUsername(token)).thenReturn("testuser");

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        assertEquals("existingUser", SecurityContextHolder.getContext().getAuthentication().getName());
        verify(userDetailsService, never()).loadUserByUsername(anyString());
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_UserNotFound_ContinuesFilterChain() throws ServletException, IOException {
        // Given
        String token = "validToken";
        String authHeader = "Bearer " + token;

        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(jwtUtil.extractUsername(token)).thenReturn("nonexistent");
        when(userDetailsService.loadUserByUsername("nonexistent"))
            .thenThrow(new org.springframework.security.core.userdetails.UsernameNotFoundException("User not found"));

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_TokenWithWhitespace_ParsesCorrectly() throws ServletException, IOException {
        // Given
        String token = "validToken";
        String authHeader = "Bearer  " + token; // Extra space

        when(request.getHeader("Authorization")).thenReturn(authHeader);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_CaseInsensitiveBearer_NotRecognized() throws ServletException, IOException {
        // Given
        when(request.getHeader("Authorization")).thenReturn("bearer token123");

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain, times(1)).doFilter(request, response);
        verify(jwtUtil, never()).extractUsername(anyString());
    }
}

