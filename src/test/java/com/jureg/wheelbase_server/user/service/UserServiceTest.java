package com.jureg.wheelbase_server.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.jureg.wheelbase_server.community_type.model.CommunityType;
import com.jureg.wheelbase_server.follow.model.Follow;
import com.jureg.wheelbase_server.follow.repository.FollowRepository;
import com.jureg.wheelbase_server.shared.api.exception.FieldAlreadyExistsException;
import com.jureg.wheelbase_server.shared.api.exception.InvalidCredentialsException;
import com.jureg.wheelbase_server.shared.api.exception.UserNotFoundException;
import com.jureg.wheelbase_server.shared.service.JwtService;
import com.jureg.wheelbase_server.user.dto.UserAuthDto;
import com.jureg.wheelbase_server.user.dto.UserAuthResponseDto;
import com.jureg.wheelbase_server.user.dto.UserCreateDto;
import com.jureg.wheelbase_server.user.dto.UserPasswordUpdateDto;
import com.jureg.wheelbase_server.user.dto.UserResponseDto;
import com.jureg.wheelbase_server.user.dto.UserSummaryDto;
import com.jureg.wheelbase_server.user.dto.UserUpdateDto;
import com.jureg.wheelbase_server.user.mapper.UserMapper;
import com.jureg.wheelbase_server.user.model.AccountType;
import com.jureg.wheelbase_server.user.model.User;
import com.jureg.wheelbase_server.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

	@Mock
	private UserRepository userRepository;
	@Mock
	private FollowRepository followRepository;
	@Mock
	private UserMapper userMapper;
	@Mock
	private PasswordEncoder passwordEncoder;
	@Mock
	private JwtService jwtService;
	
	@InjectMocks
	private UserService userService;
	
	private User currentUser;
	private UUID currentUserId;
	
	@BeforeEach
	void setupUser() {
		currentUserId = UUID.randomUUID();
		currentUser = new User();
		currentUser.setId(currentUserId);
		currentUser.setUsername("John");
		currentUser.setEmail("John@test.com");
		currentUser.setPassword("hashed-password");
		currentUser.setCommunities(Set.of(CommunityType.CAR));
	}
	
	// -------------------------------------------------------------
	// Create user
	// -------------------------------------------------------------
	@Test
	void givenUser_whenCreateUser_thenReturnUser() {
		UserCreateDto dto = new UserCreateDto("John", "John@test.com", "A bio", "password", AccountType.FOLLOWERS_ONLY, Set.of(CommunityType.CAR));
		
		// Mock the repositories
		when(userRepository.existsByEmailIgnoreCase(dto.email())).thenReturn(false);
		when(userRepository.existsByUsernameIgnoreCase(dto.username())).thenReturn(false);
		when(userMapper.toEntity(dto)).thenReturn(currentUser);
		when(passwordEncoder.encode(dto.password())).thenReturn("hashed-password");
		when(userMapper.toResponseDto(currentUser)).thenReturn(new UserResponseDto(currentUserId, "John", "John@test.com", "", "A bio", AccountType.FOLLOWERS_ONLY, Set.of(CommunityType.CAR)));
		
		// Check the create user action works
		UserResponseDto createdUser = userService.createUser(dto);
		
		assertThat(createdUser.username()).isEqualTo("John");
		assertThat(createdUser.email()).isEqualTo("John@test.com");
		assertThat(createdUser.avatarUrl()).isEqualTo("");
		assertThat(createdUser.bio()).isEqualTo("A bio");
		assertThat(createdUser.communities()).isEqualTo(Set.of(CommunityType.CAR));
		
		// Make sure the functions from userRepository have been called correctly
		verify(userRepository).existsByEmailIgnoreCase(any(String.class));
		verify(userRepository).existsByUsernameIgnoreCase(any(String.class));
		verify(userMapper).toEntity(any(UserCreateDto.class));
		verify(passwordEncoder).encode(any(String.class));
		verify(userMapper).toResponseDto(any(User.class));
		verify(userRepository).save(any(User.class));
	}
	
	@Test
	void givenUserEmailAlreadyExists_whenCreateEmail_thenException() {
		UserCreateDto dto = new UserCreateDto("John", "John@test.com", "A bio", "password", AccountType.FOLLOWERS_ONLY, Set.of(CommunityType.CAR));
		
		// Mock the repositories
		when(userRepository.existsByEmailIgnoreCase(dto.email())).thenReturn(true);

		// Make sure creating a user with an existing username fails
		FieldAlreadyExistsException exception = assertThrows(FieldAlreadyExistsException.class, () -> userService.createUser(dto));

		// Check we got the right error message
		assertEquals("Email already exists", exception.getMessage());
		// Make sure the userMapper and passwordEncoder was never used
		verifyNoInteractions(userMapper, passwordEncoder);
		// Make sure the "save" function from userRepository has been never called
		verify(userRepository, never()).save(any());
	}
	
	@Test
	void givenUsernameAlreadyExists_whenCreateUser_thenException() {
		UserCreateDto dto = new UserCreateDto("John", "John@test.com", "A bio", "password", AccountType.FOLLOWERS_ONLY, Set.of(CommunityType.CAR));
		
		// Mock the repositories
		when(userRepository.existsByEmailIgnoreCase(dto.email())).thenReturn(false);
		when(userRepository.existsByUsernameIgnoreCase(dto.username())).thenReturn(true);

		// Make sure creating a user with an existing username fails
		FieldAlreadyExistsException exception = assertThrows(FieldAlreadyExistsException.class, () -> userService.createUser(dto));

		// Check we got the right error message
		assertEquals("Username already exists", exception.getMessage());
		// Make sure the userMapper and passwordEncoder were never used
		verifyNoInteractions(userMapper, passwordEncoder);
		// Make sure the "save" function from userRepository has been never called
		verify(userRepository, never()).save(any());
	}
	
	// -------------------------------------------------------------
	// Read users
	// -------------------------------------------------------------
	@Test
	void givenUserId_whenFindUser_thenReturnUser() {
		// Mock the repositories
		when(userRepository.findById(currentUserId)).thenReturn(Optional.of(currentUser));
		when(userMapper.toResponseDto(currentUser)).thenReturn(new UserResponseDto(currentUserId, "John", "John@test.com", "avatar", "A bio", AccountType.FOLLOWERS_ONLY, Set.of(CommunityType.CAR)));
		
		UserResponseDto foundUser = userService.getUserById(currentUserId);
		
		assertThat(foundUser.username()).isEqualTo("John");
		assertThat(foundUser.email()).isEqualTo("John@test.com");
		assertThat(foundUser.avatarUrl()).isEqualTo("avatar");
		assertThat(foundUser.bio()).isEqualTo("A bio");
		assertThat(foundUser.communities()).isEqualTo(Set.of(CommunityType.CAR));
	}
	
	@Test
	void givenUnknownUserId_whenFindUser_thenException() {
		// Mock the repositories
		when(userRepository.findById(currentUserId)).thenReturn(Optional.empty());
		
		// Make sure getting a user with unknown ID fails
		UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> userService.getUserById(currentUserId));
		
		// Check we got the right error message
		assertEquals("User not found", exception.getMessage());
		// Make sure the userMapper was never used
		verifyNoInteractions(userMapper);
		// Make sure the "findById" function from userRepository has been called once
		verify(userRepository).findById(currentUserId);
	}
	
	@Test
	void givenUsername_whenFindUsersByUsername_thenReturnListUsers() {
		// Mock the repositories
		when(userRepository.findByUsernameContainingIgnoreCase("Jo")).thenReturn(List.of(currentUser));
		when(userMapper.toSummaryDto(currentUser)).thenReturn(new UserSummaryDto(currentUserId, "John", "avatar", AccountType.FOLLOWERS_ONLY));
		
		// Make sure searching users work
		List<UserSummaryDto> foundUsers = userService.searchUsersByUsername("Jo");
		
		assertThat(foundUsers).hasSize(1);
		assertThat(foundUsers.get(0).id()).isEqualTo(currentUserId);
		assertThat(foundUsers.get(0).username()).isEqualTo("John");
		assertThat(foundUsers.get(0).avatarUrl()).isEqualTo("avatar");
	}
	
	@Test
	void givenUsername_whenFindUsersByUsernameNothingFound_thenReturnEmptyList() {
		// Mock the repositories
		when(userRepository.findByUsernameContainingIgnoreCase("Jo")).thenReturn(List.of());
		
		// Make sure searching users work
		List<UserSummaryDto> foundUsers = userService.searchUsersByUsername("Jo");
		
		assertThat(foundUsers).hasSize(0);
	}
	
	@Test
	void givenEmptyUsername_whenFindUsers_thenReturnEmptyList() {
		// Make sure searching users work
		List<UserSummaryDto> foundUsers = userService.searchUsersByUsername("");
		assertThat(foundUsers).hasSize(0);
		// Make sure the userRepository was never used
		verifyNoInteractions(userRepository);
	}
	
	@Test
	void givenNullUsername_whenFindUsers_thenReturnEmptyList() {
		// Make sure searching users work
		List<UserSummaryDto> foundUsers = userService.searchUsersByUsername(null);
		assertThat(foundUsers).hasSize(0);
		// Make sure the userRepository was never used
		verifyNoInteractions(userRepository);
	}
	
	// -------------------------------------------------------------
	// Update user
	// -------------------------------------------------------------
	@Test
	void givenUpdateData_whenUpdateUser_thenReturnUpdatedUser() {
		UserUpdateDto dto = new UserUpdateDto("New John", "New avatar", "New bio", AccountType.PUBLIC, Set.of(CommunityType.MOTORBIKE, CommunityType.CAR));
		
		// Mock the repositories
		when(userRepository.findById(currentUserId)).thenReturn(Optional.of(currentUser));
		when(userMapper.toResponseDto(currentUser)).thenReturn(new UserResponseDto(currentUserId, "New John", "John@test.com", "New avatar", "New bio", AccountType.PUBLIC, Set.of(CommunityType.MOTORBIKE, CommunityType.CAR)));
		
		// Update the user
		UserResponseDto updatedUser = userService.updateUser(currentUserId, dto);
		
		assertThat(updatedUser.username()).isEqualTo("New John");
		assertThat(currentUser.getUsername()).isEqualTo("New John");
		assertThat(updatedUser.avatarUrl()).isEqualTo("New avatar");
		assertThat(currentUser.getAvatarUrl()).isEqualTo("New avatar");
		assertThat(updatedUser.bio()).isEqualTo("New bio");
		assertThat(currentUser.getBio()).isEqualTo("New bio");
		assertThat(updatedUser.accountType()).isEqualTo(AccountType.PUBLIC);
		assertThat(currentUser.getAccountType()).isEqualTo(AccountType.PUBLIC);
		assertThat(updatedUser.communities()).isEqualTo(Set.of(CommunityType.MOTORBIKE, CommunityType.CAR));
		assertThat(currentUser.getCommunities()).isEqualTo(Set.of(CommunityType.MOTORBIKE, CommunityType.CAR));
	}
	
	@Test
	void givenEmptyUpdateData_whenUpdateUser_thenReturnUnchangedUser() {
		UserUpdateDto dto = new UserUpdateDto(null, null, null, null, null);
		
		// Mock the repositories
		when(userRepository.findById(currentUserId)).thenReturn(Optional.of(currentUser));
		when(userMapper.toResponseDto(currentUser)).thenReturn(new UserResponseDto(currentUserId, "John", "John@test.com", null, null, AccountType.FOLLOWERS_ONLY, Set.of(CommunityType.CAR)));
		
		// Update the user
		UserResponseDto updatedUser = userService.updateUser(currentUserId, dto);
		
		assertThat(updatedUser.username()).isEqualTo("John");
		assertThat(currentUser.getUsername()).isEqualTo("John");
		assertThat(updatedUser.avatarUrl()).isEqualTo(null);
		assertThat(currentUser.getAvatarUrl()).isEqualTo(null);
		assertThat(updatedUser.bio()).isEqualTo(null);
		assertThat(currentUser.getBio()).isEqualTo(null);
		assertThat(updatedUser.accountType()).isEqualTo(AccountType.FOLLOWERS_ONLY);
		assertThat(currentUser.getAccountType()).isEqualTo(AccountType.FOLLOWERS_ONLY);
		assertThat(updatedUser.communities()).isEqualTo(Set.of(CommunityType.CAR));
		assertThat(currentUser.getCommunities()).isEqualTo(Set.of(CommunityType.CAR));
	}
	
	@Test
	void givenUnknownUserId_whenUpdateUser_thenException() {
		UserUpdateDto dto = new UserUpdateDto("John", "avatar", "bio", AccountType.FOLLOWERS_ONLY, Set.of(CommunityType.MOTORBIKE));
		
		// Mock the repositories
		when(userRepository.findById(currentUserId)).thenReturn(Optional.empty());
		
		// Make sure updating a non-existent user fails
		UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> userService.updateUser(currentUserId, dto));
		
		// Check we got the right error message
		assertEquals("User not found", exception.getMessage());
		// Make sure the userMapper was never used
		verifyNoInteractions(userMapper);
	}
	
	@Test
	void givenNewPassword_whenChangePassword_thenReturnUpdatedUser() {
		UserPasswordUpdateDto dto = new UserPasswordUpdateDto("password", "new-password");
		
		// Mock the repositories
		when(userRepository.findById(currentUserId)).thenReturn(Optional.of(currentUser));
		when(passwordEncoder.matches("password", "hashed-password")).thenReturn(true);
		when(passwordEncoder.encode("new-password")).thenReturn("new-hashed-password");
		
		// Make sure the changePassword function works
		userService.changePassword(currentUserId, dto);
		
		assertThat(currentUser.getPassword()).isEqualTo("new-hashed-password");
	}
	
	@Test
	void givenWrongOldPassword_whenChangePassword_thenException() {
		UserPasswordUpdateDto dto = new UserPasswordUpdateDto("password", "new-password");
		
		// Mock the repositories
		when(userRepository.findById(currentUserId)).thenReturn(Optional.of(currentUser));
		when(passwordEncoder.matches("password", "hashed-password")).thenReturn(false);
		
		// Make sure the changePassword function with non-matching passwords fails
		InvalidCredentialsException exception = assertThrows(InvalidCredentialsException.class, () -> userService.changePassword(currentUserId, dto));
		
		// Check we got the right error message
		assertEquals("Invalid credentials", exception.getMessage());
		verify(userRepository).findById(any(UUID.class));
		verify(passwordEncoder).matches("password", "hashed-password");
		verify(passwordEncoder, never()).encode(any(String.class));
	}
	
	@Test
	void givenUnknownUserId_whenChangePassword_thenException() {
		UserPasswordUpdateDto dto = new UserPasswordUpdateDto("password", "new-password");
		
		// Mock the repositories
		when(userRepository.findById(currentUserId)).thenReturn(Optional.empty());

		// Make sure the changePassword function with unknown user fails
		UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> userService.changePassword(currentUserId, dto));
		
		// Check we got the right error message
		assertEquals("User not found", exception.getMessage());
		// Make sure the passwordEncoder was never used
		verifyNoInteractions(passwordEncoder);
	}
	
	// -------------------------------------------------------------
	// Delete user
	// -------------------------------------------------------------
	@Test
	void givenUserId_whenDeleteUser_thenSuccess() {
		// Mock the repositories
		when(userRepository.findById(currentUserId)).thenReturn(Optional.of(currentUser));
		
		// Make sure the deleteUser function works
		userService.deleteUser(currentUserId);
		
		verify(userRepository).findById(currentUserId);
		verify(userRepository).delete(currentUser);
	}
	
	@Test
	void givenUnknownUserId_whenDeleteUser_thenException() {
		// Mock the repositories
		when(userRepository.findById(currentUserId)).thenReturn(Optional.empty());
		
		// Make sure the deleteUser function fails with an unknown user
		UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> userService.deleteUser(currentUserId));
		
		// Check we got the right error message
		assertEquals("User not found", exception.getMessage());
		verify(userRepository).findById(currentUserId);
		verify(userRepository, never()).delete(any(User.class));
	}
	
	// -------------------------------------------------------------
	// Authenticate user
	// -------------------------------------------------------------
	@Test
	void givenAuthData_whenAuthUser_thenReturnJwtToken() {
		UserAuthDto dto = new UserAuthDto("John@test.com", "password");
		
		// Mock the repositories
		when(userRepository.findByUsernameIgnoreCaseOrEmailIgnoreCase(dto.usernameOrEmail(), dto.usernameOrEmail())).thenReturn(Optional.of(currentUser));
		when(passwordEncoder.matches(dto.password(), currentUser.getPassword())).thenReturn(true);
		when(jwtService.generateToken(currentUser)).thenReturn("jwt-token");
		when(userMapper.toResponseDto(currentUser)).thenReturn(
			new UserResponseDto(currentUserId, currentUser.getUsername(), currentUser.getEmail(), currentUser.getAvatarUrl(), currentUser.getBio(), AccountType.FOLLOWERS_ONLY, Set.of(CommunityType.CAR))
		);
		
		// Make sure the authUser function works correctly
		UserAuthResponseDto response = userService.authUser(dto);
		
		assertThat(response.jwt()).isEqualTo("jwt-token");
		assertThat(response.user().id()).isEqualTo(currentUserId);
		assertThat(response.user().username()).isEqualTo("John");
	}
	
	@Test
	void givenWrongPassword_whenAuthUser_thenException() {
		UserAuthDto dto = new UserAuthDto("John@test.com", "password");
		
		// Mock the repositories
		when(userRepository.findByUsernameIgnoreCaseOrEmailIgnoreCase(dto.usernameOrEmail(), dto.usernameOrEmail())).thenReturn(Optional.of(currentUser));
		when(passwordEncoder.matches(dto.password(), currentUser.getPassword())).thenReturn(false);
		
		// Make sure the authUser function detects a wrong password
		InvalidCredentialsException exception = assertThrows(InvalidCredentialsException.class, () -> userService.authUser(dto));
		
		// Check we got the right error message
		assertEquals("Invalid credentials", exception.getMessage());
		verify(userRepository).findByUsernameIgnoreCaseOrEmailIgnoreCase(dto.usernameOrEmail(), dto.usernameOrEmail());
		verify(passwordEncoder).matches(any(String.class), any(String.class));
		verifyNoInteractions(jwtService, userMapper);
	}
	
	@Test
	void givenUnknownId_whenAuthUser_thenException() {
		UserAuthDto dto = new UserAuthDto("John@test.com", "password");
		
		// Mock the repositories
		when(userRepository.findByUsernameIgnoreCaseOrEmailIgnoreCase(dto.usernameOrEmail(), dto.usernameOrEmail())).thenReturn(Optional.empty());
		
		// Make sure the authUser function detects a non-existent user
		UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> userService.authUser(dto));
		
		// Check we got the right error message
		assertEquals("User not found", exception.getMessage());
		verify(userRepository).findByUsernameIgnoreCaseOrEmailIgnoreCase(dto.usernameOrEmail(), dto.usernameOrEmail());
		verifyNoInteractions(jwtService, userMapper, passwordEncoder);
	}
	
	// -------------------------------------------------------------
	// Get followers / following
	// -------------------------------------------------------------
	@Test
	void givenUser_whenGetFollowers_thenReturnListUsers() {
		UUID follower1Id = UUID.randomUUID();
		User follower1 = createUser(follower1Id, "Alice");
		UUID follower2Id = UUID.randomUUID();
		User follower2 = createUser(follower2Id, "Frank");
		
		Follow follow1 = new Follow(follower1, currentUser, Instant.now());
		Follow follow2 = new Follow(follower2, currentUser, Instant.now());
		
		// Mock the repositories
		when(userRepository.findById(currentUserId)).thenReturn(Optional.of(currentUser));
		when(followRepository.findByFollowed(currentUser)).thenReturn(List.of(follow1, follow2));
		when(userMapper.toSummaryDto(follower1)).thenReturn(new UserSummaryDto(follower1Id, "Alice", null, AccountType.FOLLOWERS_ONLY));
		when(userMapper.toSummaryDto(follower2)).thenReturn(new UserSummaryDto(follower2Id, "Frank", null, AccountType.FOLLOWERS_ONLY));
		
		// Make sure we get all the followers
		List<UserSummaryDto> followers = userService.getFollowers(currentUserId);
		
		assertThat(followers).hasSize(2);
		assertThat(followers.get(0).id()).isEqualTo(follower1Id);
		assertThat(followers.get(0).username()).isEqualTo("Alice");
		assertThat(followers.get(1).id()).isEqualTo(follower2Id);
		assertThat(followers.get(1).username()).isEqualTo("Frank");
	}
	
	@Test
	void givenUserWithoutFollowers_whenGetFollowers_thenReturnEmptyList() {
		// Mock the repositories
		when(userRepository.findById(currentUserId)).thenReturn(Optional.of(currentUser));
		when(followRepository.findByFollowed(currentUser)).thenReturn(List.of());
		
		// Make sure we get all the followers
		List<UserSummaryDto> followers = userService.getFollowers(currentUserId);
		
		assertThat(followers).hasSize(0);
		verifyNoInteractions(userMapper);
	}
	
	@Test
	void givenUnknowUser_whenGetFollowers_thenException() {
		// Mock the repositories
		when(userRepository.findById(currentUserId)).thenReturn(Optional.empty());

		// Make sure we detect the user is unknown
		UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> userService.getFollowers(currentUserId));
		
		// Check we got the right error message
		assertEquals("User not found", exception.getMessage());
		verify(userRepository).findById(any(UUID.class));
		verifyNoInteractions(userMapper, followRepository);
	}
	
	@Test
	void givenUser_whenGetFollowing_thenReturnListUsers() {
		UUID followed1Id = UUID.randomUUID();
		User followed1 = createUser(followed1Id, "Alice");
		UUID followed2Id = UUID.randomUUID();
		User followed2 = createUser(followed2Id, "Frank");
		
		Follow follow1 = new Follow(currentUser, followed1, Instant.now());
		Follow follow2 = new Follow(currentUser, followed2, Instant.now());
		
		// Mock the repositories
		when(userRepository.findById(currentUserId)).thenReturn(Optional.of(currentUser));
		when(followRepository.findByFollower(currentUser)).thenReturn(List.of(follow1, follow2));
		when(userMapper.toSummaryDto(followed1)).thenReturn(new UserSummaryDto(followed1Id, "Alice", null, AccountType.FOLLOWERS_ONLY));
		when(userMapper.toSummaryDto(followed2)).thenReturn(new UserSummaryDto(followed2Id, "Frank", null, AccountType.FOLLOWERS_ONLY));
		
		// Make sure we get all the followers
		List<UserSummaryDto> followers = userService.getFollowing(currentUserId);
		
		assertThat(followers).hasSize(2);
		assertThat(followers.get(0).id()).isEqualTo(followed1Id);
		assertThat(followers.get(0).username()).isEqualTo("Alice");
		assertThat(followers.get(1).id()).isEqualTo(followed2Id);
		assertThat(followers.get(1).username()).isEqualTo("Frank");
	}
	
	@Test
	void givenUserWithoutFollowing_whenGetFollowing_thenReturnEmptyList() {
		// Mock the repositories
		when(userRepository.findById(currentUserId)).thenReturn(Optional.of(currentUser));
		when(followRepository.findByFollower(currentUser)).thenReturn(List.of());
		
		// Make sure we get all the followers
		List<UserSummaryDto> followers = userService.getFollowing(currentUserId);
		
		assertThat(followers).hasSize(0);
		verifyNoInteractions(userMapper);
	}
	
	@Test
	void givenUnknowUser_whenGetFollowing_thenException() {
		// Mock the repositories
		when(userRepository.findById(currentUserId)).thenReturn(Optional.empty());

		// Make sure we detect the user is unknown
		UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> userService.getFollowing(currentUserId));
		
		// Check we got the right error message
		assertEquals("User not found", exception.getMessage());
		verify(userRepository).findById(any(UUID.class));
		verifyNoInteractions(userMapper, followRepository);
	}
	
	// -------------------------------------------------------------
	// Helper method(s)
	// -------------------------------------------------------------
	private User createUser(UUID id, String username) {
		User newUser = new User();
		newUser.setId(id);
		newUser.setUsername(username);
		newUser.setEmail(username + "@test.com");
		newUser.setPassword("password");
		return newUser;
	}
	
}
