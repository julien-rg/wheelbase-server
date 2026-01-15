package com.jureg.wheelbase_server.user.dto;

import java.util.UUID;

import com.jureg.wheelbase_server.user.model.AccountType;

public record UserSummaryDto(
		
		UUID id,
		String username,
		String avatarUrl,
		AccountType accountType
		
) {
	
}
