package com.jureg.wheelbase_server.follow.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.jureg.wheelbase_server.follow.model.Follow;
import com.jureg.wheelbase_server.follow.repository.FollowRepository;
import com.jureg.wheelbase_server.shared.api.exception.AlreadyFollowingException;
import com.jureg.wheelbase_server.shared.api.exception.CannotFollowYourselfException;
import com.jureg.wheelbase_server.shared.api.exception.CannotUnfollowYourselfException;
import com.jureg.wheelbase_server.shared.api.exception.NotFollowingUserException;
import com.jureg.wheelbase_server.shared.api.exception.UserNotFoundException;
import com.jureg.wheelbase_server.user.model.User;
import com.jureg.wheelbase_server.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class FollowServiceTest {
	
	@Mock
	private UserRepository userRepository;
	@Mock
	private FollowRepository followRepository;
	
	@InjectMocks
	private FollowService followService;
	
	private UUID currentUserId;
	
	@BeforeEach
	void setupSecurityContext() {
		currentUserId = UUID.randomUUID();
		
		SecurityContext context = SecurityContextHolder.createEmptyContext();
		context.setAuthentication(
			new UsernamePasswordAuthenticationToken(currentUserId, null, List.of())
		);
		SecurityContextHolder.setContext(context);
	}
	
	// -------------------------------------------------------------
	// Follow user
	// -------------------------------------------------------------
	@Test
	void givenUserId_whenFollowUser_thenSuccess() {
		UUID followedId = UUID.randomUUID();
		
		User follower = new User();
		follower.setId(currentUserId);
		
		User followed = new User();
		followed.setId(followedId);
		
		// Mock the repositories
		when(userRepository.findById(currentUserId)).thenReturn(Optional.of(follower));
		when(userRepository.findById(followedId)).thenReturn(Optional.of(followed));
		when(followRepository.existsByFollowerIdAndFollowedId(currentUserId, followedId)).thenReturn(false);
		
		// Check that the follow action works
		followService.followUser(followedId);
		
		// Make sure the "save" function from followRepository has been called once
		verify(followRepository).save(any(Follow.class));
	}
	
	@Test
	void givenUserId_whenFollowHimself_thenException() {
		CannotFollowYourselfException exception = assertThrows(CannotFollowYourselfException.class, () -> followService.followUser(currentUserId));
		
		// Check we got the right error message
		assertEquals("Cannot follow yourself", exception.getMessage());
		// Make sure the repositories were never used
		verifyNoInteractions(userRepository, followRepository);
	}
	
	@Test
	void givenUserId_whenAlreadyFollow_thenException() {
		UUID followedId = UUID.randomUUID();
		
		// Mock the repositories
		when(followRepository.existsByFollowerIdAndFollowedId(currentUserId, followedId)).thenReturn(true);
		
		AlreadyFollowingException exception = assertThrows(AlreadyFollowingException.class, () -> followService.followUser(followedId));
		
		// Check we got the right error message
		assertEquals("Already following this user", exception.getMessage());
		// Make sure the repository was never used
		verifyNoInteractions(userRepository);
		// Make sure the "save" function from followRepository has been never called
		verify(followRepository, never()).save(any());
	}
	
	@Test
	void givenUserId_whenFollowerNotFound_thenException() {
		UUID followedId = UUID.randomUUID();
		
		User follower = new User();
		follower.setId(currentUserId);
		
		User followed = new User();
		followed.setId(followedId);
		
		// Mock the repositories
		when(userRepository.findById(currentUserId)).thenReturn(Optional.empty());
		when(followRepository.existsByFollowerIdAndFollowedId(currentUserId, followedId)).thenReturn(false);
		
		UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> followService.followUser(followedId));
		
		// Check we got the right error message
		assertEquals("Follower user not found", exception.getMessage());
	}
	
	@Test
	void givenUserId_whenFollowedNotFound_thenException() {
		UUID followedId = UUID.randomUUID();
		
		User follower = new User();
		follower.setId(currentUserId);
		
		User followed = new User();
		followed.setId(followedId);
		
		// Mock the repositories
		when(userRepository.findById(currentUserId)).thenReturn(Optional.of(follower));
		when(userRepository.findById(followedId)).thenReturn(Optional.empty());
		when(followRepository.existsByFollowerIdAndFollowedId(currentUserId, followedId)).thenReturn(false);
		
		UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> followService.followUser(followedId));
		
		// Check we got the right error message
		assertEquals("Followed user not found", exception.getMessage());
	}
	
	// -------------------------------------------------------------
	// Unfollow user
	// -------------------------------------------------------------
	@Test
	void givenUserId_whenUnfollow_thenSuccess() {
		UUID followedId = UUID.randomUUID();
		
		User follower = new User();
		follower.setId(currentUserId);
		
		User followed = new User();
		followed.setId(followedId);
		
		// Mock the repositories
		when(userRepository.findById(currentUserId)).thenReturn(Optional.of(follower));
		when(userRepository.findById(followedId)).thenReturn(Optional.of(followed));
		when(followRepository.existsByFollowerIdAndFollowedId(currentUserId, followedId)).thenReturn(true);
		
		// Check that the unfollow action works
		followService.unfollowUser(followedId);
		
		// Make sure the "existsByFollowerIdAndFollowedId" function from followRepository has been called once
		verify(followRepository).existsByFollowerIdAndFollowedId(currentUserId, followedId);
		// Make sure the "deleteByFollowerIdAndFollowedId" function from followRepository has been called once
		verify(followRepository).deleteByFollowerIdAndFollowedId(currentUserId, followedId);
	}
	
	@Test
	void givenUserId_whenUnfollowHimself_thenException() {
		CannotUnfollowYourselfException exception = assertThrows(CannotUnfollowYourselfException.class, () -> followService.unfollowUser(currentUserId));
		
		// Check we got the right error message
		assertEquals("Cannot unfollow yourself", exception.getMessage());
		// Make sure the repositories were never used
		verifyNoInteractions(userRepository, followRepository);
	}
	
	@Test
	void givenUserId_whenUnfollowNotFollowing_thenException() {
		UUID followedId = UUID.randomUUID();
		
		User follower = new User();
		follower.setId(currentUserId);
		
		User followed = new User();
		followed.setId(followedId);
		
		// Mock the repositories
		when(followRepository.existsByFollowerIdAndFollowedId(currentUserId, followedId)).thenReturn(false);
		when(userRepository.findById(currentUserId)).thenReturn(Optional.of(follower));
		when(userRepository.findById(followedId)).thenReturn(Optional.of(followed));
		
		NotFollowingUserException exception = assertThrows(NotFollowingUserException.class, () -> followService.unfollowUser(followedId));
		
		// Check we got the right error message
		assertEquals("Not following this user", exception.getMessage());
		// Make sure the "deleteByFollowerIdAndFollowedId" function from followRepository has been never called
		verify(followRepository, never()).deleteByFollowerIdAndFollowedId(any(), any());
	}
	
	@Test
	void givenUserId_whenUnfollowFollowerNotFound_thenException() {
		UUID followedId = UUID.randomUUID();
		
		User follower = new User();
		follower.setId(currentUserId);
		
		User followed = new User();
		followed.setId(followedId);
		
		// Mock the repositories
		when(userRepository.findById(currentUserId)).thenReturn(Optional.empty());
		
		UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> followService.unfollowUser(followedId));
		
		// Check we got the right error message
		assertEquals("Follower user not found", exception.getMessage());
	}
	
	@Test
	void givenUserId_whenUnfollowFollowedNotFound_thenException() {
		UUID followedId = UUID.randomUUID();
		
		User follower = new User();
		follower.setId(currentUserId);
		
		User followed = new User();
		followed.setId(followedId);
		
		// Mock the repositories
		when(userRepository.findById(currentUserId)).thenReturn(Optional.of(follower));
		when(userRepository.findById(followedId)).thenReturn(Optional.empty());
		
		UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> followService.unfollowUser(followedId));
		
		// Check we got the right error message
		assertEquals("Followed user not found", exception.getMessage());
	}

}











