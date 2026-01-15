package com.jureg.wheelbase_server.user.dto;

import java.util.Set;

import com.jureg.wheelbase_server.community_type.model.CommunityType;
import com.jureg.wheelbase_server.user.model.AccountType;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UserCreateDto(
		
		@Size(min = 3, max = 30)
		@NotBlank
		String username,
		
		@Email
		@NotBlank
		String email,
		
		@Size(min = 0, max = 255)
		String bio,
		
		@Size(min = 8, max = 100)
		@NotBlank
		String password,
		
		@NotNull
		AccountType accountType,
		
		@NotNull
		Set<CommunityType> communities
		
) {

}
