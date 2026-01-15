package com.jureg.wheelbase_server.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserAuthDto(
		
		@NotBlank
		String usernameOrEmail,
		
		@NotBlank
		@Size(min = 8, max = 100)
		String password
		
) {

}
