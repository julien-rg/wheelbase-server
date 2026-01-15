package com.jureg.wheelbase_server.user.dto;

import java.util.Set;

import com.jureg.wheelbase_server.community_type.model.CommunityType;
import com.jureg.wheelbase_server.user.model.AccountType;

import jakarta.validation.constraints.Size;

public record UserUpdateDto(
		
		@Size(min = 3, max = 30)
		String username,
		
		String avatarUrl,
		
		@Size(min = 0, max = 255)
		String bio,
		
		AccountType accountType,

		@Size(min = 1, message = "At least one community is required")
		Set<CommunityType> communities
		
) {

}
