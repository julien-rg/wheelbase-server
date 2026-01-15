package com.jureg.wheelbase_server.shared.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

class SecurityUtilsTest {

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
	void givenLoggedUser_whenGetCurrentUserId_thenReturnUserId() {
		// Simulate a user is authenticated
		UUID userId = UUID.randomUUID();
		TestingAuthenticationToken auth = new TestingAuthenticationToken(userId, null);
		auth.setAuthenticated(true);
		SecurityContextHolder.getContext().setAuthentication(auth);
		
		// Get user ID from JWT
		UUID resultId = SecurityUtils.getCurrentUserId();
		
		assertThat(resultId).isEqualTo(userId);
	}
	
	@Test
	void givenNotLoggedUser_whenGetCurrentUserId_thenException() {
		TestingAuthenticationToken auth = new TestingAuthenticationToken(UUID.randomUUID(), null);
		auth.setAuthenticated(false);
		SecurityContextHolder.getContext().setAuthentication(auth);
		
		// Make sure getting the user ID from JWT throws an exception
		IllegalStateException exception = assertThrows(IllegalStateException.class, () -> SecurityUtils.getCurrentUserId());
		
		// Check we got the right error message
		assertEquals("User is not authenticated", exception.getMessage());
	}
	
	@Test
	void givenNoAuth_whenGetCurrentUserId_thenException() {
		// Make sure getting the user ID from JWT throws an exception
		IllegalStateException exception = assertThrows(IllegalStateException.class, () -> SecurityUtils.getCurrentUserId());
		
		// Check we got the right error message
		assertEquals("User is not authenticated", exception.getMessage());
	}
	
	@Test
	void givenLoggedUserWithoutValidPrincipal_whenGetCurrentUserId_thenException() {
		// Simulate a user is authenticated, but with a different principal
		TestingAuthenticationToken auth = new TestingAuthenticationToken("not-a-UUID", null);
		auth.setAuthenticated(true);
		SecurityContextHolder.getContext().setAuthentication(auth);
		
		// Make sure getting the user ID from JWT throws an exception
		IllegalStateException exception = assertThrows(IllegalStateException.class, () -> SecurityUtils.getCurrentUserId());
		
		// Check we got the right error message
		assertEquals("Unable to extract user ID from authentication", exception.getMessage());
	}
	
}
