package com.jureg.wheelbase_server.user.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jureg.wheelbase_server.follow.service.FollowService;
import com.jureg.wheelbase_server.user.dto.UserAuthDto;
import com.jureg.wheelbase_server.user.dto.UserAuthResponseDto;
import com.jureg.wheelbase_server.user.dto.UserCreateDto;
import com.jureg.wheelbase_server.user.dto.UserFollowDto;
import com.jureg.wheelbase_server.user.dto.UserPasswordUpdateDto;
import com.jureg.wheelbase_server.user.dto.UserResponseDto;
import com.jureg.wheelbase_server.user.dto.UserSummaryDto;
import com.jureg.wheelbase_server.user.dto.UserUpdateDto;
import com.jureg.wheelbase_server.user.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
public class UserController {

	private final UserService userService;
	private final FollowService followService;
	
	public UserController(UserService userService,
			FollowService followService) {
		this.userService = userService;
		this.followService = followService;
	}
	
	// -------------------------------------------------------------
	// Register user
	// -------------------------------------------------------------
	@PostMapping("/register")
	public ResponseEntity<UserResponseDto> register(@Valid @RequestBody UserCreateDto dto) {
		UserResponseDto user = userService.createUser(dto);
		return ResponseEntity.ok(user);
	}
	
	// -------------------------------------------------------------
	// Authenticate / login user
	// -------------------------------------------------------------
	@PostMapping("/login")
	public ResponseEntity<UserAuthResponseDto> login(@Valid @RequestBody UserAuthDto dto) {
		UserAuthResponseDto user = userService.authUser(dto);
		// Generate cookie for JWT
		ResponseCookie cookie = ResponseCookie.from("jwt", user.jwt())
			.httpOnly(true)
			.secure(true)
			.path("/")
			.maxAge(24 * 60 * 60)	// 24 hours expiration
			.sameSite("Strict")
			.build();
		return ResponseEntity.ok().header("Set-Cookie", cookie.toString()).body(user);
	}
	
	// -------------------------------------------------------------
	// Fetch user profile
	// -------------------------------------------------------------
	@GetMapping("/{id}")
	public ResponseEntity<UserResponseDto> getUser(@PathVariable UUID id) {
		UserResponseDto user = userService.getUserById(id);
		return ResponseEntity.ok(user);
	}
	
	// -------------------------------------------------------------
	// Update user profile
	// -------------------------------------------------------------
	@PutMapping("/{id}")
	public ResponseEntity<UserResponseDto> updateUser(@PathVariable UUID id, @Valid @RequestBody UserUpdateDto dto) {
		UserResponseDto user = userService.updateUser(id, dto);
		return ResponseEntity.ok(user);
	}
	
	// -------------------------------------------------------------
	// Update user password
	// -------------------------------------------------------------
	@PutMapping("/{id}/password")
	public ResponseEntity<Void> changePassword(@PathVariable UUID id, @Valid @RequestBody UserPasswordUpdateDto dto) {
		userService.changePassword(id, dto);
		return ResponseEntity.noContent().build();
	}

	// -------------------------------------------------------------
	// Search users by name
	// -------------------------------------------------------------
	@GetMapping
	public ResponseEntity<List<UserSummaryDto>> searchUsersByUsername(@RequestParam(required = true) String username) {
		List<UserSummaryDto> users = userService.searchUsersByUsername(username);
		return ResponseEntity.ok(users);
	}
	
	// -------------------------------------------------------------
	// Follow / Un-follow
	// -------------------------------------------------------------
	@PostMapping("/follow")
	public ResponseEntity<Void> followUser(@Valid @RequestBody UserFollowDto dto) {
		followService.followUser(dto.followedId());
		return ResponseEntity.noContent().build();
	}
	@PostMapping("/unfollow")
	public ResponseEntity<Void> unfollowUser(@Valid @RequestBody UserFollowDto dto) {
		followService.unfollowUser(dto.followedId());
		return ResponseEntity.noContent().build();
	}
	
	// -------------------------------------------------------------
	// Get followers / following user(s)
	// -------------------------------------------------------------
	@GetMapping("/{id}/followers")
	public ResponseEntity<List<UserSummaryDto>> getFollowers(@PathVariable UUID id) {
		return ResponseEntity.ok(userService.getFollowers(id));
	}
	@GetMapping("/{id}/following")
	public ResponseEntity<List<UserSummaryDto>> getFollowing(@PathVariable UUID id) {
		return ResponseEntity.ok(userService.getFollowing(id));
	}
	
}
