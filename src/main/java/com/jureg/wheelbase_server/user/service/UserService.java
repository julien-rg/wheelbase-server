package com.jureg.wheelbase_server.user.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jureg.wheelbase_server.follow.model.Follow;
import com.jureg.wheelbase_server.follow.repository.FollowRepository;
import com.jureg.wheelbase_server.shared.api.exception.FieldAlreadyExistsException;
import com.jureg.wheelbase_server.shared.api.exception.InvalidCredentialsException;
import com.jureg.wheelbase_server.shared.api.exception.UserNotFoundException;
import com.jureg.wheelbase_server.shared.service.JwtService;
import com.jureg.wheelbase_server.user.dto.UserAuthResponseDto;
import com.jureg.wheelbase_server.user.dto.UserCreateDto;
import com.jureg.wheelbase_server.user.dto.UserAuthDto;
import com.jureg.wheelbase_server.user.dto.UserPasswordUpdateDto;
import com.jureg.wheelbase_server.user.dto.UserResponseDto;
import com.jureg.wheelbase_server.user.dto.UserSummaryDto;
import com.jureg.wheelbase_server.user.dto.UserUpdateDto;
import com.jureg.wheelbase_server.user.mapper.UserMapper;
import com.jureg.wheelbase_server.user.model.User;
import com.jureg.wheelbase_server.user.repository.UserRepository;

@Service
@Transactional
public class UserService {
	
	private final UserRepository userRepository;
	private final UserMapper userMapper;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;
	private final FollowRepository followRepository;
	
	public UserService(UserRepository userRepository,
			UserMapper userMapper,
			PasswordEncoder passwordEncoder,
			JwtService jwtService,
			FollowRepository followRepository) {
		this.userRepository = userRepository;
		this.userMapper = userMapper;
		this.passwordEncoder = passwordEncoder;
		this.jwtService = jwtService;
		this.followRepository = followRepository;
	}
	
	// -------------------------------------------------------------
	// Create
	// -------------------------------------------------------------
	public UserResponseDto createUser(UserCreateDto dto) {
		if (userRepository.existsByEmailIgnoreCase(dto.email())) {
			throw new FieldAlreadyExistsException("Email");
		}
		if (userRepository.existsByUsernameIgnoreCase(dto.username())) {
			throw new FieldAlreadyExistsException("Username");
		}
		User newUser = userMapper.toEntity(dto);
		// Hash password
		newUser.setPassword(passwordEncoder.encode(dto.password()));
		userRepository.save(newUser);
		return userMapper.toResponseDto(newUser);
	}
	
	// -------------------------------------------------------------
	// Read
	// -------------------------------------------------------------
	@Transactional(readOnly = true)
	@PreAuthorize("@userSecurity.isUserPublicOrYourselfOrFollowed(#id)")
	public UserResponseDto getUserById(UUID id) {
		User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException());
		return userMapper.toResponseDto(user);
	}
	@Transactional(readOnly = true)
	public List<UserSummaryDto> searchUsersByUsername(String query) {
		if (query == null || query.isBlank()) {
			return List.of();
		}
		return userRepository.findByUsernameContainingIgnoreCase(query).stream().map(userMapper::toSummaryDto).collect(Collectors.toList());
	}
	
	// -------------------------------------------------------------
	// Update
	// -------------------------------------------------------------
	@PreAuthorize("@userSecurity.isUserYourself(#id)")
	public UserResponseDto updateUser(UUID id, UserUpdateDto dto) {
		User existing = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException());
		if (dto.username() != null) {
			// Check if new user name already exists
			boolean newUsernameTaken = userRepository.findByUsernameIgnoreCase(dto.username()).isPresent();
			if (newUsernameTaken) {
				throw new FieldAlreadyExistsException("Username");
			}
			existing.setUsername(dto.username());
		}
		if (dto.avatarUrl() != null) {
			existing.setAvatarUrl(dto.avatarUrl());
		}
		if (dto.bio() != null) {
			existing.setBio(dto.bio());
		}
		if (dto.accountType() != null) {
			existing.setAccountType(dto.accountType());
		}
		if (dto.communities() != null) {
			existing.setCommunities(dto.communities());
		}
		return userMapper.toResponseDto(existing);
	}
	public void changePassword(UUID id, UserPasswordUpdateDto dto) {
		User existing = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException());
		// Check if old password matches
		if (!passwordEncoder.matches(dto.oldPassword(), existing.getPassword())) {
			throw new InvalidCredentialsException();		
		}
		// Hash password
		existing.setPassword(passwordEncoder.encode(dto.newPassword()));
	}
	
	// -------------------------------------------------------------
	// Delete
	// -------------------------------------------------------------
	public void deleteUser(UUID id) {
		User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException());
		userRepository.delete(user);
	}
	
	// -------------------------------------------------------------
	// Authenticate user
	// -------------------------------------------------------------
	public UserAuthResponseDto authUser(UserAuthDto dto) {
		// Check if user exists
		User user = userRepository.findByUsernameIgnoreCaseOrEmailIgnoreCase(dto.usernameOrEmail(), dto.usernameOrEmail())
				.orElseThrow(() -> new UserNotFoundException());
		// Check if password matches
		if (!passwordEncoder.matches(dto.password(), user.getPassword())) {
			throw new InvalidCredentialsException();
		}
		// Generate a JWT
		String jwt = jwtService.generateToken(user);
		return new UserAuthResponseDto(jwt, userMapper.toResponseDto(user));
	}
	
	// -------------------------------------------------------------
	// Get followers / following user(s)
	// -------------------------------------------------------------
	public List<UserSummaryDto> getFollowers(UUID userId) {
		User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException());
		return followRepository.findByFollowed(user)
				.stream()
				.map(Follow::getFollower)
				.map(userMapper::toSummaryDto)
				.collect(Collectors.toList());
	}
	public List<UserSummaryDto> getFollowing(UUID userId) {
		User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException());
		return followRepository.findByFollower(user)
				.stream()
				.map(Follow::getFollowed)
				.map(userMapper::toSummaryDto)
				.collect(Collectors.toList());
	}
	
}









