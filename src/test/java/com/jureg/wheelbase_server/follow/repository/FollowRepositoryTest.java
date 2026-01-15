package com.jureg.wheelbase_server.follow.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.jureg.wheelbase_server.community_type.model.CommunityType;
import com.jureg.wheelbase_server.follow.model.Follow;
import com.jureg.wheelbase_server.user.repository.UserRepository;
import com.jureg.wheelbase_server.user.model.User;

@DataJpaTest
@Testcontainers
class FollowRepositoryTest {
	
	@SuppressWarnings("resource")
	@Container
	static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:18")
		.withDatabaseName("wheelbase")
		.withUsername("test")
		.withPassword("test");
	
	@DynamicPropertySource
	static void overrideDatasource(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", postgres::getJdbcUrl);
		registry.add("spring.datasource.username", postgres::getUsername);
		registry.add("spring.datasource.password", postgres::getPassword);
	}
	
	@AfterAll
	static void tearDown() {
		postgres.close();
	}
	
	@Autowired
	private FollowRepository followRepository;
	
	@Autowired
	private UserRepository userRepository;
	
	// -------------------------------------------------------------
	// Find follows
	// -------------------------------------------------------------
	@Test
	void givenUser_whenFindByFollower_thenReturnFollows() {
		User follower = createUser("John");
		User followed = createUser("Alice");
		
		Follow follow = new Follow(follower, followed, Instant.now());
		followRepository.saveAndFlush(follow);
		
		List<Follow> followsFromFollower = followRepository.findByFollower(follower);
		assertThat(followsFromFollower).hasSize(1);
		assertThat(followsFromFollower.get(0).getFollower().getId()).isEqualTo(follower.getId());
		assertThat(followsFromFollower.get(0).getFollowed().getId()).isEqualTo(followed.getId());
	}
	@Test
	void givenUser_whenFindByFollowed_thenReturnFollows() {
		User follower = createUser("John");
		User followed = createUser("Alice");
		
		Follow follow = new Follow(follower, followed, Instant.now());
		followRepository.saveAndFlush(follow);
		
		List<Follow> followsFromFollowed = followRepository.findByFollowed(followed);
		assertThat(followsFromFollowed).hasSize(1);
		assertThat(followsFromFollowed.get(0).getFollower().getId()).isEqualTo(follower.getId());
		assertThat(followsFromFollowed.get(0).getFollowed().getId()).isEqualTo(followed.getId());
	}
	
	// -------------------------------------------------------------
	// Follow exists
	// -------------------------------------------------------------
	@Test
	void givenFollowerIdAndFollowedId_whenExists_thenReturnTrue() {
		User follower = createUser("John");
		User followed = createUser("Alice");
		
		followRepository.saveAndFlush(new Follow(follower, followed, Instant.now()));
		boolean exists = followRepository.existsByFollowerIdAndFollowedId(follower.getId(), followed.getId());
		assertThat(exists).isTrue();
	}
	
	@Test
	void givenFollowerIdAndFollowedId_whenNotExists_thenReturnFalse() {
		User follower = createUser("John");
		User followed = createUser("Alice");

		boolean exists = followRepository.existsByFollowerIdAndFollowedId(follower.getId(), followed.getId());
		assertThat(exists).isFalse();
	}
	
	// -------------------------------------------------------------
	// Unfollow
	// -------------------------------------------------------------
	@Test
	void givenFollowerIdAndFollowedId_whenDelete_thenShouldDelete() {
		User follower = createUser("John");
		User followed = createUser("Alice");
		
		followRepository.saveAndFlush(new Follow(follower, followed, Instant.now()));
		followRepository.deleteByFollowerIdAndFollowedId(follower.getId(), followed.getId());
		
		List<Follow> followsFromFollower = followRepository.findByFollower(follower);
		assertThat(followsFromFollower).hasSize(0);
		List<Follow> followsFromFollowed = followRepository.findByFollower(follower);
		assertThat(followsFromFollowed).hasSize(0);
	}
	
	// -------------------------------------------------------------
	// Helper method(s)
	// -------------------------------------------------------------
	private User createUser(String username) {
		User user = new User();
		user.setUsername(username);
		user.setEmail(username + "@test.com");
		user.setPassword("password");
		user.setCommunities(Set.of(CommunityType.CAR));
		return userRepository.saveAndFlush(user);
	}

}













