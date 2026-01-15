package com.jureg.wheelbase_server.shared.security;

import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {

	/**
	 * Get the current authenticated user ID from SecurityContext
	 * @return UUID of the current user
	 * @throws IllegalStateException if no user is authenticated
	 */
	public static UUID getCurrentUserId() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		
		if (authentication == null || !authentication.isAuthenticated()) {
			throw new IllegalStateException("User is not authenticated");
		}
		
        // Extracts the principal (a UUID) set by JwtAuthenticationFilter
		Object principal = authentication.getPrincipal();
		if (principal instanceof UUID) {
			return (UUID) principal;
		}
		
		throw new IllegalStateException("Unable to extract user ID from authentication");
	}
}

