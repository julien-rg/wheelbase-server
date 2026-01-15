package com.jureg.wheelbase_server.user.dto;

public record UserAuthResponseDto(
		
		String jwt,
		
		UserSummaryDto user
		
) {

}
