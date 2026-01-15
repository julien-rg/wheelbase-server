package com.jureg.wheelbase_server.shared.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

	private final JwtAuthenticationFilter jwtAuthenticationFilter;

	public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
		this.jwtAuthenticationFilter = jwtAuthenticationFilter;
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.csrf(csrf -> csrf.disable())
			.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.authorizeHttpRequests(auth -> auth

				// Public end points – GET
	            .requestMatchers(HttpMethod.GET,
	                "/api/users",
	                "/api/users/{id}",
	                "/api/users/{id}/followers",
	                "/api/users/{id}/following"
	            ).permitAll()

	            // Public end points – PUT
//	            .requestMatchers(HttpMethod.PUT,
//	            	"/api/users/{id}"
//	            ).permitAll()

	            // Public end points – POST
	            .requestMatchers(HttpMethod.POST,
	                "/api/users/register",
	                "/api/users/login"
	            ).permitAll()

				// All other end points require authentication
				.anyRequest().authenticated()
			)
			.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
			.exceptionHandling(ex -> ex
			    .authenticationEntryPoint((request, response, authException) -> {
			        // 401 Unauthorized
			        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
			    })
			    .accessDeniedHandler((request, response, accessDeniedException) -> {
			        // 403 Forbidden
			        response.sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden");
			    })
			);

		return http.build();
	}
}

