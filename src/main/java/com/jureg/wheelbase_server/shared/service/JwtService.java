package com.jureg.wheelbase_server.shared.service;

import java.util.Date;
import java.util.UUID;

import javax.crypto.SecretKey;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.jureg.wheelbase_server.user.model.User;

@Service
public class JwtService {
	
	// Token duration
	private static final long EXPIRATION_MS = 1000 * 60 * 60; // 60 minutes
	
	private final SecretKey key;
	
	public JwtService(@Value("${jwt.secret}") String secretKey) {
		this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
	}
	
	// -------------------------------------------------------------
	// Generate token
	// -------------------------------------------------------------
	/**
	 * Generate a JWT for the user
	 * @param User we want to generate the JWT token
	 * @return A JWT string
	 */
	public String generateToken(User user) {
		Date now = new Date();
		Date expirationDate = new Date(now.getTime() + EXPIRATION_MS);
		return Jwts.builder()
				.subject(user.getId().toString())
				.issuedAt(now)
				.expiration(expirationDate)
				.signWith(key, Jwts.SIG.HS256)
				.compact();
	}
	
	// -------------------------------------------------------------
	// Validate token
	// -------------------------------------------------------------
	/**
	 * Validate a JWT
	 * @param The JWT token we want to check
	 * @return true if valid, else false
	 */
	public boolean validateToken(String token) {
		try {
			Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	// -------------------------------------------------------------
	// Extract subject from token
	// -------------------------------------------------------------
	/**
	 * Return the subject (UUID) of a JWT
	 * @param The JWT token we want to get subject from
	 * @return The subject of the token (UUID)
	 */
	public UUID getUserIdFromToken(String token) {
		Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
		return UUID.fromString(claims.getSubject());
	}
}
