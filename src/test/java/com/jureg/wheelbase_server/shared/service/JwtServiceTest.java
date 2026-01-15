package com.jureg.wheelbase_server.shared.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.jsonwebtoken.MalformedJwtException;

import com.jureg.wheelbase_server.user.model.User;

class JwtServiceTest {

	private JwtService jwtService;
	private User user;
	
	@BeforeEach
	void setup() {
		jwtService = new JwtService("mysupersecretkeymysupersecretkey123456");
		user = new User();
		user.setId(UUID.randomUUID());
		user.setUsername("John");
		user.setEmail("John@test.com");
		user.setPassword("password");
	}
	
	// -------------------------------------------------------------
	// Generate token
	// -------------------------------------------------------------
	@Test
	void givenUser_whenGenerateToken_thenReturnJwt() {
		String jwt = jwtService.generateToken(user);
		assertThat(jwt).isNotNull();
	}
	
	// -------------------------------------------------------------
	// Validate token
	// -------------------------------------------------------------
	@Test
	void givenValidJwt_whenValidateToken_thenReturnTrue() {
		String jwt = jwtService.generateToken(user);
		boolean isValid = jwtService.validateToken(jwt);
		assertThat(isValid).isTrue();
	}
	
	@Test
	void givenInvalidJwt_whenValidateToken_thenReturnFalse() {
		boolean isValid = jwtService.validateToken("an-invalid-jwt");
		assertThat(isValid).isFalse();
	}
	
	@Test
	void givenBrokenJwt_whenValidateToken_thenReturnFalse() {
		String jwt = jwtService.generateToken(user);
		// Replace last character with an "x" to break the JWT
		String brokenJwt = jwt.substring(0, jwt.length() - 1) + "xx";
		boolean isValid = jwtService.validateToken(brokenJwt);
		assertThat(isValid).isFalse();
	}
	
	// -------------------------------------------------------------
	// Extract subject from token
	// -------------------------------------------------------------
	@Test
	void givenValidJwt_whenGetUserIdFromToken_thenReturnUserId() {
		UUID userId = UUID.randomUUID();
		user.setId(userId);
		String jwt = jwtService.generateToken(user);
		UUID userIdFound = jwtService.getUserIdFromToken(jwt);
		assertThat(userIdFound).isEqualTo(userId);
	}
	
	@Test
	void givenInvalidJwt_whenGetUserIdFromToken_thenException() {
		MalformedJwtException exception = assertThrows(MalformedJwtException.class, () -> jwtService.getUserIdFromToken("an-invalid-jwt"));
		// Check we got the right error message
		assertTrue(exception.getMessage().contains("Invalid compact JWT"), "Exception message should mention 'Invalid compact JWT'");
	}
	
}
