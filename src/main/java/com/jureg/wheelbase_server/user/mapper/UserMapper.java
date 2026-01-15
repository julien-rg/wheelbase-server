package com.jureg.wheelbase_server.user.mapper;

import org.springframework.stereotype.Component;

import com.jureg.wheelbase_server.user.dto.UserCreateDto;
import com.jureg.wheelbase_server.user.dto.UserResponseDto;
import com.jureg.wheelbase_server.user.dto.UserSummaryDto;
import com.jureg.wheelbase_server.user.dto.UserUpdateDto;
import com.jureg.wheelbase_server.user.model.User;

@Component
public class UserMapper {

	// -------------------------------------------------------------
	// User -> UserResponseDto
	// -------------------------------------------------------------
	public UserResponseDto toResponseDto(User user) {
		return new UserResponseDto(
			user.getId(),
			user.getUsername(),
			user.getEmail(),
			user.getAvatarUrl(),
			user.getBio(),
			user.getAccountType(),
			user.getCommunities()
		);
	}
	
	// -------------------------------------------------------------
	// User -> UserSummaryDto
	// -------------------------------------------------------------
	public UserSummaryDto toSummaryDto(User user) {
		return new UserSummaryDto(
			user.getId(),
			user.getUsername(),
			user.getAvatarUrl(),
			user.getAccountType()
		);
	}
	
	// -------------------------------------------------------------
	// UserCreateDto -> User
	// -------------------------------------------------------------
	public User toEntity(UserCreateDto user) {
		return User.builder()
			.username(user.username())
			.email(user.email())
			.bio(user.bio())
			.password(user.password())
			.accountType(user.accountType())
			.communities(user.communities())
			.build();
	}
	
	// -------------------------------------------------------------
	// UserUpdateDto -> User
	// -------------------------------------------------------------
	public void updateEntity(User user, UserUpdateDto dto) {
		if (dto.username() != null) {
			user.setUsername(dto.username());
		}
		if (dto.avatarUrl() != null) {
			user.setAvatarUrl(dto.avatarUrl());
		}
		if (dto.bio() != null) {
			user.setBio(dto.bio());
		}
		if (dto.accountType() != null) {
			user.setAccountType(dto.accountType());
		}
		if (dto.communities() != null) {
			user.setCommunities(dto.communities());
		}
	}
	
}
