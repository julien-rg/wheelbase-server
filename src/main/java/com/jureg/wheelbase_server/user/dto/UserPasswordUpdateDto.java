package com.jureg.wheelbase_server.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserPasswordUpdateDto(
		
		@NotBlank
		String oldPassword,
		
		@NotBlank
		@Size(min = 8, max = 100)
		String newPassword
		
) {

}
