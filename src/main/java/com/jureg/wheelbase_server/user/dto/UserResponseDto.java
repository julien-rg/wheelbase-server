package com.jureg.wheelbase_server.user.dto;

import java.util.Set;
import java.util.UUID;

import com.jureg.wheelbase_server.community_type.model.CommunityType;
import com.jureg.wheelbase_server.user.model.AccountType;

public record UserResponseDto(
		
		UUID id,
		String username,
		String email,
		String avatarUrl,
		String bio,
		AccountType accountType,
		Set<CommunityType> communities
		
) {

}
