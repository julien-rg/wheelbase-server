package com.jureg.wheelbase_server.user.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record UserFollowDto(
		
		@NotNull
		UUID followedId
		
) {

}
