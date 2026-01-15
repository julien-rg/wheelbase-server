package com.jureg.wheelbase_server.shared.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.jureg.wheelbase_server.shared.service.JwtService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

	@Mock
	private JwtService jwtService;
	@Mock
	private HttpServletRequest request;
	@Mock
	private HttpServletResponse response;
	@Mock
	private FilterChain filterChain;
	
	@InjectMocks
	private JwtAuthenticationFilter filter;
	
	@BeforeEach
	void clearContextBefore() {
		SecurityContextHolder.clearContext();
	}
	@AfterEach
	void clearContextAfter() {
		SecurityContextHolder.clearContext();
	}
	
	// -------------------------------------------------------------
	// Authentication
	// -------------------------------------------------------------
	@Test
	void givenValidJwt_whenAuthUser_thenUserAuthenticated() throws Exception {
		UUID userId = UUID.randomUUID();
		String validJwt = "valid.jwt.token";
		
		// Mock HttpServletRequest and JwtService
		when(request.getHeader("Authorization")).thenReturn("Bearer " + validJwt);
		when(jwtService.validateToken(validJwt)).thenReturn(true);
		when(jwtService.getUserIdFromToken(validJwt)).thenReturn(userId);
		
		// Trigger authentication
		filter.doFilter(request, response, filterChain);
		
		// Get the authentication from context
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		
		// Make sure it's there
		assertThat(auth).isNotNull();
		assertThat(UUID.fromString(auth.getPrincipal().toString())).isEqualTo(userId);
		// Make sure the "doFilter" function has been called
		verify(filterChain).doFilter(request, response);
	}
	
	@Test
	void givenInvalidAuthScheme_whenAuthUser_thenUserNotAuthenticated() throws Exception {
		String validJwt = "valid.jwt.token";
		
		// Mock HttpServletRequest and JwtService
		when(request.getHeader("Authorization")).thenReturn("Invalid " + validJwt);
		
		// Trigger authentication
		filter.doFilter(request, response, filterChain);
		
		// Get the authentication from context
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		
		// Make sure authenticated is null
		assertThat(auth).isNull();
		// Make sure the token has not been validated
		verify(jwtService, never()).validateToken(validJwt);
		// Make sure the "doFilter" function has been called
		verify(filterChain).doFilter(request, response);
	}
	
	@Test
	void givenNullAuthHeader_whenAuthUser_thenUserNotAuthenticated() throws Exception {
		// Mock HttpServletRequest and JwtService
		when(request.getHeader("Authorization")).thenReturn(null);
		
		// Trigger authentication
		filter.doFilter(request, response, filterChain);
		
		// Get the authentication from context
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		
		// Make sure authenticated is null
		assertThat(auth).isNull();
		// Make sure the token has not been validated
		verify(jwtService, never()).validateToken(any());
		// Make sure the "doFilter" function has been called
		verify(filterChain).doFilter(request, response);
	}
	
	@Test
	void givenInvalidJwt_whenAuthUser_thenUserAuthenticated() throws Exception {
		String invalidJwt = "invalid.jwt.token";
		
		// Mock HttpServletRequest and JwtService
		when(request.getHeader("Authorization")).thenReturn("Bearer " + invalidJwt);
		when(jwtService.validateToken(invalidJwt)).thenReturn(false);
		
		// Trigger authentication
		filter.doFilter(request, response, filterChain);
		
		// Get the authentication from context
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		
		// Make sure it's there
		assertThat(auth).isNull();
		// Make sure we never called "getUserIdFromToken" function
		verify(jwtService, never()).getUserIdFromToken(invalidJwt);
		// Make sure the "doFilter" function has been called
		verify(filterChain).doFilter(request, response);
	}
}
