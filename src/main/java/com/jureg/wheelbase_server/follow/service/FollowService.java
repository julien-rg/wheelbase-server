package com.jureg.wheelbase_server.follow.service;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jureg.wheelbase_server.follow.model.Follow;
import com.jureg.wheelbase_server.follow.repository.FollowRepository;
import com.jureg.wheelbase_server.shared.api.exception.AlreadyFollowingException;
import com.jureg.wheelbase_server.shared.api.exception.CannotFollowYourselfException;
import com.jureg.wheelbase_server.shared.api.exception.CannotUnfollowYourselfException;
import com.jureg.wheelbase_server.shared.api.exception.NotFollowingUserException;
import com.jureg.wheelbase_server.shared.api.exception.UserNotFoundException;
import com.jureg.wheelbase_server.shared.security.SecurityUtils;
import com.jureg.wheelbase_server.user.model.User;
import com.jureg.wheelbase_server.user.repository.UserRepository;

@Service
@Transactional
public class FollowService {
	
	private final UserRepository userRepository;
	private final FollowRepository followRepository;
	
	public FollowService(UserRepository userRepository,
			FollowRepository followRepository) {
		this.userRepository = userRepository;
		this.followRepository = followRepository;
	}

	// -------------------------------------------------------------
	// Follow / Unfollow user
	// -------------------------------------------------------------
	public void followUser(UUID followedId) {
		UUID currentUserId = SecurityUtils.getCurrentUserId();
		// A user cannot follow himself
		if (currentUserId.equals(followedId)) {
			throw new CannotFollowYourselfException();
		}
		// Check if user is already following this user
		if (followRepository.existsByFollowerIdAndFollowedId(currentUserId, followedId)) {
			throw new AlreadyFollowingException();
		}
		// Fetch users
		User follower = userRepository.findById(currentUserId).orElseThrow(() -> new UserNotFoundException("Follower user not found"));
		User followed = userRepository.findById(followedId).orElseThrow(() -> new UserNotFoundException("Followed user not found"));
		// Set the follow
		Follow follow = new Follow();
		follow.setFollower(follower);
		follow.setFollowed(followed);
		follow.setCreatedAt(Instant.now());
		// Save the follow in database
		followRepository.save(follow);
	}
	public void unfollowUser(UUID followedId) {
		UUID currentUserId = SecurityUtils.getCurrentUserId();
		// A user cannot follow himself
		if (currentUserId.equals(followedId)) {
			throw new CannotUnfollowYourselfException();
		}
		// Fetch users
		userRepository.findById(currentUserId).orElseThrow(() -> new UserNotFoundException("Follower user not found"));
		userRepository.findById(followedId).orElseThrow(() -> new UserNotFoundException("Followed user not found"));
		// Check if user is already following this user
		if (!followRepository.existsByFollowerIdAndFollowedId(currentUserId, followedId)) {
			throw new NotFollowingUserException();
		}
		followRepository.deleteByFollowerIdAndFollowedId(currentUserId, followedId);
	}
		
}
