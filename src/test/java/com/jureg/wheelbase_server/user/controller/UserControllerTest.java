package com.jureg.wheelbase_server.user.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.jureg.wheelbase_server.community_type.model.CommunityType;
import com.jureg.wheelbase_server.follow.model.Follow;
import com.jureg.wheelbase_server.follow.repository.FollowRepository;
import com.jureg.wheelbase_server.user.model.AccountType;
import com.jureg.wheelbase_server.user.model.User;
import com.jureg.wheelbase_server.user.repository.UserRepository;


@SpringBootTest(properties = {"jwt.secret=mysupersecretkeymysupersecretkey123456"})
@Testcontainers
@AutoConfigureMockMvc(addFilters = true)
class UserControllerTest {
	
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
	
	@BeforeEach
	void setup() {
		// Clean DB
		followRepository.deleteAll();
		userRepository.deleteAll();
		// Create new user
	    user1 = new User();
	    user1.setUsername("Alice");
	    user1.setEmail("Alice@test.com");
	    user1.setPassword(passwordEncoder.encode("password"));
	    user1.setId(userRepository.save(user1).getId());
	    // Create new user
	    user2 = new User();
	    user2.setUsername("Tom");
	    user2.setEmail("Tom@test.com");
	    user2.setPassword(passwordEncoder.encode("another-password"));
	    user2.setId(userRepository.save(user2).getId());
	    // Create new user
	    user3 = new User();
	    user3.setUsername("Mark");
	    user3.setEmail("Mark@test.com");
	    user3.setAvatarUrl("avatar");
	    user3.setBio("Mark is cool");
	    user3.setPassword(passwordEncoder.encode("a-cool-password"));
	    user3.setAccountType(AccountType.PUBLIC);
	    user3.setId(userRepository.save(user3).getId());
	    // Create new user
	    user4 = new User();
	    user4.setUsername("Patrick");
	    user4.setEmail("Patrick@test.com");
	    user4.setPassword(passwordEncoder.encode("another-cool-password"));
	    user4.setId(userRepository.save(user4).getId());
	    // Create new user
	    user5 = new User();
	    user5.setUsername("Patricia");
	    user5.setEmail("Patricia@test.com");
	    user5.setPassword(passwordEncoder.encode("awesome-password"));
	    user5.setId(userRepository.save(user5).getId());
	    // User4 is following User2 and User3
	    followRepository.save(new Follow(user4, user2, Instant.now()));
	    followRepository.save(new Follow(user4, user3, Instant.now()));
	    // User5 is following User2
	    followRepository.save(new Follow(user5, user2, Instant.now()));
	    // User3 is following User2
	    followRepository.save(new Follow(user3, user2, Instant.now()));
	}

	@Autowired
	private MockMvcTester mockMvcTester;
	
	@Autowired
    private PasswordEncoder passwordEncoder;
	
	@Autowired
    private UserRepository userRepository;
	@Autowired
    private FollowRepository followRepository;
	
	private User user1;
	private User user2;
	private User user3;
	private User user4;
	private User user5;
   
	// -------------------------------------------------------------
	// Register user
	// -------------------------------------------------------------
	@Test
	void givenValidUserCreateDto_whenRegistering_thenReturnCreatedUser() throws Exception {
		String dto = String.format("""
			{ "username": "John", "email": "John@test.com", "bio": "A bio", "password": "password", "accountType": "%s", "communities": [ "%s" ] }
		""", AccountType.FOLLOWERS_ONLY, CommunityType.CAR);

		// Simulate a HTTP call to register a new user
		assertThat(mockMvcTester.post().uri("/api/users/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(dto))
		        .hasStatus(HttpStatus.OK)
		        .bodyJson()
		        .hasPath("$.id")
		        .hasPath("$.username")
		        .hasPath("$.email")
		        .hasPath("$.communities")
		        .hasPathSatisfying("$.username", value -> assertThat(value).isEqualTo("John"))
		        .hasPathSatisfying("$.email", value -> assertThat(value).isEqualTo("John@test.com"))
		        .hasPathSatisfying("$.bio", value -> assertThat(value).isEqualTo("A bio"));
	}
	
	@Test
	void givenAlreadyExistingUsername_whenRegistering_thenReturnError() {
		String dto = String.format("""
			{ "username": "Alice", "email": "Alicedoe@test.com", "bio": "A bio", "password": "password", "accountType": "%s", "communities": [ "%s" ] }
		""", AccountType.FOLLOWERS_ONLY, CommunityType.CAR);

		// Simulate a HTTP call to register a new user
		assertThat(mockMvcTester.post().uri("/api/users/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(dto))
		        .hasStatus(HttpStatus.CONFLICT)
		        .bodyJson()
		        .hasPathSatisfying("$.error", value -> assertThat(value).isIn("Username already exists"));
	}
	
	@Test
	void givenEmptyUsername_whenRegistering_thenReturnError() {
		String dto = String.format("""
			{ "username": "", "email": "John@test.com", "bio": "A bio", "password": "password", "accountType": "%s", "communities": [ "%s" ] }
		""", AccountType.FOLLOWERS_ONLY, CommunityType.CAR);

		// Simulate a HTTP call to register a new user
		assertThat(mockMvcTester.post().uri("/api/users/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(dto))
		        .hasStatus(HttpStatus.BAD_REQUEST)
		        .bodyJson()
		        .hasPathSatisfying("$.username", value -> assertThat(value).isIn("size must be between 3 and 30", "must not be blank"));
	}
	
	@Test
	void givenNoUsername_whenRegistering_thenReturnError() {
		String dto = String.format("""
			{ "email": "John@test.com", "bio": "A bio", "password": "password", "accountType": "%s", "communities": [ "%s" ] }
		""", AccountType.FOLLOWERS_ONLY, CommunityType.CAR);

		// Simulate a HTTP call to register a new user
		assertThat(mockMvcTester.post().uri("/api/users/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(dto))
		        .hasStatus(HttpStatus.BAD_REQUEST)
		        .bodyJson()
		        .hasPathSatisfying("$.username", value -> assertThat(value).isEqualTo("must not be blank"));
	}
	
	@Test
	void givenAlreadyExistingEmail_whenRegistering_thenReturnError() {
		String dto = String.format("""
			{ "username": "Alice Doe", "email": "Alice@test.com", "bio": "A bio", "password": "password", "accountType": "%s", "communities": [ "%s" ] }
		""", AccountType.FOLLOWERS_ONLY, CommunityType.CAR);

		// Simulate a HTTP call to register a new user
		assertThat(mockMvcTester.post().uri("/api/users/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(dto))
		        .hasStatus(HttpStatus.CONFLICT)
		        .bodyJson()
		        .hasPathSatisfying("$.error", value -> assertThat(value).isIn("Email already exists"));
	}
	
	@Test
	void givenInvalidEmail_whenRegistering_thenReturnError() {
		String dto = String.format("""
			{ "username": "John", "email": "Johntest.com", "bio": "A bio", "password": "password", "accountType": "%s", "communities": [ "%s" ] }
		""", AccountType.FOLLOWERS_ONLY, CommunityType.CAR);

		// Simulate a HTTP call to register a new user
		assertThat(mockMvcTester.post().uri("/api/users/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(dto))
		        .hasStatus(HttpStatus.BAD_REQUEST)
		        .bodyJson()
		        .hasPathSatisfying("$.email", value -> assertThat(value).isEqualTo("must be a well-formed email address"));
	}
	
	@Test
	void givenNoEmail_whenRegistering_thenReturnError() {
		String dto = String.format("""
			{ "username": "John", "bio": "A bio", "password": "password", "accountType": "%s", "communities": [ "%s" ] }
		""", AccountType.FOLLOWERS_ONLY, CommunityType.CAR);

		// Simulate a HTTP call to register a new user
		assertThat(mockMvcTester.post().uri("/api/users/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(dto))
		        .hasStatus(HttpStatus.BAD_REQUEST)
		        .bodyJson()
		        .hasPathSatisfying("$.email", value -> assertThat(value).isEqualTo("must not be blank"));
	}
	
	@Test
	void givenShortPassword_whenRegistering_thenReturnError() {
		String dto = String.format("""
			{ "username": "John", "email": "John@test.com", "bio": "A bio", "password": "huh", "accountType": "%s", "communities": [ "%s" ] }
		""", AccountType.FOLLOWERS_ONLY, CommunityType.CAR);

		// Simulate a HTTP call to register a new user
		assertThat(mockMvcTester.post().uri("/api/users/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(dto))
		        .hasStatus(HttpStatus.BAD_REQUEST)
		        .bodyJson()
		        .hasPathSatisfying("$.password", value -> assertThat(value).isEqualTo("size must be between 8 and 100"));
	}
	
	@Test
	void givenInvalidAccountType_whenRegistering_thenReturnError() {
		String dto = String.format("""
			{ "username": "John", "email": "John@test.com", "bio": "A bio", "password": "huh", "accountType": "INVALID_ACCOUNT_TYPE", "communities": [ "%s" ] }
		""", CommunityType.CAR);

		// Simulate a HTTP call to register a new user
		assertThat(mockMvcTester.post().uri("/api/users/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(dto))
		        .hasStatus(HttpStatus.BAD_REQUEST)
		        .bodyJson()
		        .hasPathSatisfying("$.error", value -> assertThat(value).isEqualTo("Invalid enum value"));
	}
	
	@Test
	void givenInvalidCommunityType_whenRegistering_thenReturnError() {
		String dto = String.format("""
			{ "username": "John", "email": "John@test.com", "bio": "A bio", "password": "huh", "accountType": "%s", "communities": [ "INVALID_COMMUNITY" ] }
		""", AccountType.FOLLOWERS_ONLY);

		// Simulate a HTTP call to register a new user
		assertThat(mockMvcTester.post().uri("/api/users/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(dto))
		        .hasStatus(HttpStatus.BAD_REQUEST)
		        .bodyJson()
		        .hasPathSatisfying("$.error", value -> assertThat(value).isEqualTo("Invalid enum value"));
	}
	
	// -------------------------------------------------------------
	// Login user
	// -------------------------------------------------------------
	@Test
	void givenValidUsername_whenLogin_thenReturnJwt() throws Exception {
		String dto = """
			{ "usernameOrEmail": "Alice", "password": "password" }
		""";

		// Simulate a HTTP call to log a user
		assertThat(mockMvcTester.post().uri("/api/users/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(dto))
		        .hasStatus(HttpStatus.OK)
		        .bodyJson()
		        .hasPath("$.jwt")
				.hasPath("$.user");
	}
	@Test
	void givenValidEmail_whenLogin_thenReturnJwt() throws Exception {
		String dto = """
			{ "usernameOrEmail": "alice@test.com", "password": "password" }
		""";

		// Simulate a HTTP call to log a user
		assertThat(mockMvcTester.post().uri("/api/users/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(dto))
		        .hasStatus(HttpStatus.OK)
		        .bodyJson()
		        .hasPath("$.jwt")
				.hasPath("$.user");
	}
	
	@Test
	void givenWrongPassword_whenLogin_thenReturnError() throws Exception {
		String dto = """
			{ "usernameOrEmail": "Alice", "password": "invalid-password" }
		""";

		// Simulate a HTTP call to log a user
		assertThat(mockMvcTester.post().uri("/api/users/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(dto))
		        .hasStatus(HttpStatus.UNAUTHORIZED)
		        .bodyJson()
		        .hasPathSatisfying("$.error", value -> assertThat(value).isEqualTo("Invalid credentials"));
	}
	
	@Test
	void givenInvalidUsername_whenLogin_thenReturnError() throws Exception {
		String dto = """
			{ "usernameOrEmail": "Frank", "password": "password" }
		""";

		// Simulate a HTTP call to log a user
		assertThat(mockMvcTester.post().uri("/api/users/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(dto))
		        .hasStatus(HttpStatus.NOT_FOUND)
		        .bodyJson()
		        .hasPathSatisfying("$.error", value -> assertThat(value).isEqualTo("User not found"));
	}
	
	// -------------------------------------------------------------
	// Fetch user profile
	// -------------------------------------------------------------
	@Test
	void givenOwnerUserId_whenFetchingProfile_thenReturnProfile() throws Exception {
		// Simulate a HTTP call to fetch a user's profile
		assertThat(mockMvcTester.get().uri("/api/users/{id}", user1.getId())
				.with(authentication(new UsernamePasswordAuthenticationToken(user1.getId(), null, List.of()))))
		        .hasStatus(HttpStatus.OK)
		        .bodyJson()
		        .hasPath("$.id")
		        .hasPathSatisfying("$.username", value -> assertThat(value).isEqualTo("Alice"))
		        .hasPathSatisfying("$.email", value -> assertThat(value).isEqualTo("Alice@test.com"))
		        .hasPathSatisfying("$.avatarUrl", value -> assertThat(value).isEqualTo(null))
		        .hasPathSatisfying("$.bio", value -> assertThat(value).isEqualTo(null))
		        .hasPathSatisfying("$.communities", value -> assertThat(value).asInstanceOf(InstanceOfAssertFactories.list(String.class)).contains("CAR"));
	}
	
	@Test
	void givenPublicUserId_whenFetchingProfile_thenReturnProfile() throws Exception {
		// Simulate a HTTP call to fetch a user's profile
		assertThat(mockMvcTester.get().uri("/api/users/{id}", user3.getId()))
		        .hasStatus(HttpStatus.OK)
		        .bodyJson()
		        .hasPath("$.id")
		        .hasPathSatisfying("$.username", value -> assertThat(value).isEqualTo("Mark"))
		        .hasPathSatisfying("$.email", value -> assertThat(value).isEqualTo("Mark@test.com"))
		        .hasPathSatisfying("$.avatarUrl", value -> assertThat(value).isEqualTo("avatar"))
		        .hasPathSatisfying("$.bio", value -> assertThat(value).isEqualTo("Mark is cool"))
		        .hasPathSatisfying("$.accountType", value -> assertThat(value).isEqualTo(AccountType.PUBLIC.toString()))
		        .hasPathSatisfying("$.communities", value -> assertThat(value).asInstanceOf(InstanceOfAssertFactories.list(String.class)).contains("CAR"));
	}
	
	@Test
	void givenPrivateUserIdButFollowing_whenFetchingProfile_thenReturnProfile() throws Exception {
		// Simulate a HTTP call to fetch a user's profile
		assertThat(mockMvcTester.get().uri("/api/users/{id}", user2.getId())
				.with(authentication(new UsernamePasswordAuthenticationToken(user4.getId(), null, List.of()))))
		        .hasStatus(HttpStatus.OK)
		        .bodyJson()
		        .hasPath("$.id")
		        .hasPathSatisfying("$.username", value -> assertThat(value).isEqualTo("Tom"))
		        .hasPathSatisfying("$.email", value -> assertThat(value).isEqualTo("Tom@test.com"))
		        .hasPathSatisfying("$.accountType", value -> assertThat(value).isEqualTo(AccountType.FOLLOWERS_ONLY.toString()))
		        .hasPathSatisfying("$.communities", value -> assertThat(value).asInstanceOf(InstanceOfAssertFactories.list(String.class)).contains("CAR"));
	}
	
	@Test
	void givenPublicUserIdButFollowing_whenFetchingProfile_thenReturnProfile() throws Exception {
		// Simulate a HTTP call to fetch a user's profile
		assertThat(mockMvcTester.get().uri("/api/users/{id}", user3.getId())
				.with(authentication(new UsernamePasswordAuthenticationToken(user4.getId(), null, List.of()))))
		        .hasStatus(HttpStatus.OK)
		        .bodyJson()
		        .hasPath("$.id")
		        .hasPathSatisfying("$.username", value -> assertThat(value).isEqualTo("Mark"))
		        .hasPathSatisfying("$.email", value -> assertThat(value).isEqualTo("Mark@test.com"))
		        .hasPathSatisfying("$.avatarUrl", value -> assertThat(value).isEqualTo("avatar"))
		        .hasPathSatisfying("$.bio", value -> assertThat(value).isEqualTo("Mark is cool"))
		        .hasPathSatisfying("$.accountType", value -> assertThat(value).isEqualTo(AccountType.PUBLIC.toString()))
		        .hasPathSatisfying("$.communities", value -> assertThat(value).asInstanceOf(InstanceOfAssertFactories.list(String.class)).contains("CAR"));
	}
	
	@Test
	void givenPrivateUserIdAndNotFollowing_whenFetchingProfile_thenReturnError() throws Exception {
		// Simulate a HTTP call to fetch a user's profile
		assertThat(mockMvcTester.get().uri("/api/users/{id}", user1.getId()))
		        .hasStatus(HttpStatus.UNAUTHORIZED);
	}

	@Test
	void givenPrivateUserIdAndNotYourself_whenFetchingProfile_thenReturnError() throws Exception {
		// Simulate a HTTP call to fetch a user's profile
		assertThat(mockMvcTester.get().uri("/api/users/{id}", user2.getId())
				.with(authentication(new UsernamePasswordAuthenticationToken(user1.getId(), null, List.of()))))
		        .hasStatus(HttpStatus.UNAUTHORIZED);
	}
	
	@Test
	void givenUnknownUserId_whenFetchingProfile_thenReturnError() throws Exception {
		// Simulate a HTTP call to fetch a user's profile
		assertThat(mockMvcTester.get().uri("/api/users/{id}", UUID.randomUUID())
				.with(authentication(new UsernamePasswordAuthenticationToken(user1.getId(), null, List.of()))))
		        .hasStatus(HttpStatus.NOT_FOUND);
	}
	
	// -------------------------------------------------------------
	// Search users
	// -------------------------------------------------------------
	@Test
	void givenOneUsername_whenSearchingUsers_thenReturnListUsers() {
		// Simulate a HTTP call to search user(s)
		assertThat(mockMvcTester.get().uri("/api/users?username={username}", "Alice"))
				.hasStatus(HttpStatus.OK)
				.bodyJson()
				.hasPathSatisfying("$", value -> assertThat(value).asArray())
				.hasPathSatisfying("$.length()", value -> assertThat(value).isEqualTo(1))
				.hasPathSatisfying("$[0].username", value -> assertThat(value).isEqualTo("Alice"))
				.hasPathSatisfying("$[0].avatarUrl", value -> assertThat(value).isEqualTo(null));
	}
	
	@Test
	void givenUnknownUsername_whenSearchingUsers_thenReturnEmptyList() {
		// Simulate a HTTP call to search user(s)
		assertThat(mockMvcTester.get().uri("/api/users?username={username}", "Zed"))
				.hasStatus(HttpStatus.OK)
				.bodyJson()
				.hasPathSatisfying("$", value -> assertThat(value).asArray())
				.hasPathSatisfying("$.length()", value -> assertThat(value).isEqualTo(0));
	}
	
	@Test
	void givenMultiUsername_whenSearchingUsers_thenReturnListUsers() {
		// Simulate a HTTP call to search user(s)
		assertThat(mockMvcTester.get().uri("/api/users?username={username}", "Pat"))
				.hasStatus(HttpStatus.OK)
				.bodyJson()
				.hasPathSatisfying("$", value -> assertThat(value).asArray())
				.hasPathSatisfying("$.length()", value -> assertThat(value).isEqualTo(2));
	}
	
	@Test
	void givenEmptyUsername_whenSearchingUsers_thenReturnEmptyList() {
		// Simulate a HTTP call to search user(s)
		assertThat(mockMvcTester.get().uri("/api/users?username={username}", ""))
				.hasStatus(HttpStatus.OK)
				.bodyJson()
				.hasPathSatisfying("$", value -> assertThat(value).asArray())
				.hasPathSatisfying("$.length()", value -> assertThat(value).isEqualTo(0));
	}
	
	@Test
	void givenNoUsername_whenSearchingUsers_thenReturnError() {
		// Simulate a HTTP call to search user(s)
		assertThat(mockMvcTester.get().uri("/api/users"))
				.hasStatus(HttpStatus.BAD_REQUEST)
				.bodyJson()
				.hasPathSatisfying("$.error", value -> assertThat(value).isEqualTo("Missing required parameter"));
	}
	
	// -------------------------------------------------------------
	// Update user profile
	// -------------------------------------------------------------
	@Test
	void givenNewUsername_whenUpdatingUser_thenReturnUpdatedUser() {
		String dto = """
			{ "username": "Alicia" }
		""";

		// Simulate a HTTP call to update a user's profile
		assertThat(mockMvcTester.put().uri("/api/users/{id}", user1.getId())
				.with(authentication(new UsernamePasswordAuthenticationToken(user1.getId(), null, List.of())))
				.contentType(MediaType.APPLICATION_JSON)
				.content(dto))
		        .hasStatus(HttpStatus.OK)
		        .bodyJson()
		        .hasPathSatisfying("$.id", value -> assertThat(value).isEqualTo(user1.getId().toString()))
		        .hasPathSatisfying("$.username", value -> assertThat(value).isEqualTo("Alicia"));
	}
	
	@Test
	void givenNewAvatarUrl_whenUpdatingUser_thenReturnUpdatedUser() {
		String dto = """
			{ "avatarUrl": "alices_avatar" }
		""";

		// Simulate a HTTP call to update a user's profile
		assertThat(mockMvcTester.put().uri("/api/users/{id}", user1.getId())
				.with(authentication(new UsernamePasswordAuthenticationToken(user1.getId(), null, List.of())))
				.contentType(MediaType.APPLICATION_JSON)
				.content(dto))
		        .hasStatus(HttpStatus.OK)
		        .bodyJson()
		        .hasPathSatisfying("$.id", value -> assertThat(value).isEqualTo(user1.getId().toString()))
		        .hasPathSatisfying("$.avatarUrl", value -> assertThat(value).isEqualTo("alices_avatar"));
	}
	
	@Test
	void givenNewBio_whenUpdatingUser_thenReturnUpdatedUser() {
		String dto = """
			{ "bio": "Alice's adventures" }
		""";

		// Simulate a HTTP call to update a user's profile
		assertThat(mockMvcTester.put().uri("/api/users/{id}", user1.getId())
				.with(authentication(new UsernamePasswordAuthenticationToken(user1.getId(), null, List.of())))
				.contentType(MediaType.APPLICATION_JSON)
				.content(dto))
		        .hasStatus(HttpStatus.OK)
		        .bodyJson()
		        .hasPathSatisfying("$.id", value -> assertThat(value).isEqualTo(user1.getId().toString()))
		        .hasPathSatisfying("$.bio", value -> assertThat(value).isEqualTo("Alice's adventures"));
	}
	
	@Test
	void givenNewAccountType_whenUpdatingUser_thenReturnUpdatedUser() {
		String dto = String.format("""
			{ "accountType": "%s" }
		""", AccountType.PUBLIC);

		// Simulate a HTTP call to update a user's profile
		assertThat(mockMvcTester.put().uri("/api/users/{id}", user1.getId())
				.with(authentication(new UsernamePasswordAuthenticationToken(user1.getId(), null, List.of())))
				.contentType(MediaType.APPLICATION_JSON)
				.content(dto))
		        .hasStatus(HttpStatus.OK)
		        .bodyJson()
		        .hasPathSatisfying("$.id", value -> assertThat(value).isEqualTo(user1.getId().toString()))
		        .hasPathSatisfying("$.accountType", value -> assertThat(value).isEqualTo(AccountType.PUBLIC.toString()));
	}
	
	@Test
	void givenNewCommunities_whenUpdatingUser_thenReturnUpdatedUser() {
		String dto = String.format("""
			{ "communities": ["%s", "%s"] }
		""", CommunityType.CAR, CommunityType.MOTORBIKE);

		// Simulate a HTTP call to update a user's profile
		assertThat(mockMvcTester.put().uri("/api/users/{id}", user1.getId())
				.with(authentication(new UsernamePasswordAuthenticationToken(user1.getId(), null, List.of())))
				.contentType(MediaType.APPLICATION_JSON)
				.content(dto))
		        .hasStatus(HttpStatus.OK)
		        .bodyJson()
		        .hasPathSatisfying("$.communities", value -> assertThat(value).asArray())
				.hasPathSatisfying("$.communities.length()", value -> assertThat(value).isEqualTo(2))
				.hasPathSatisfying("$.communities", value -> assertThat(value).asInstanceOf(InstanceOfAssertFactories.list(String.class))
					.containsExactlyInAnyOrder(CommunityType.CAR.toString(), CommunityType.MOTORBIKE.toString()));
	}
	
	@Test
	void givenWrongUsername_whenUpdatingUser_thenReturnError() {
		String dto = """
			{ "username": "A" }
		""";

		// Simulate a HTTP call to update a user's profile
		assertThat(mockMvcTester.put().uri("/api/users/{id}", user1.getId())
				.with(authentication(new UsernamePasswordAuthenticationToken(user1.getId(), null, List.of())))
				.contentType(MediaType.APPLICATION_JSON)
				.content(dto))
		        .hasStatus(HttpStatus.BAD_REQUEST);
	}
	
	@Test
	void givenAlreadyExistingUsername_whenUpdatingUser_thenReturnError() {
		String dto = """
			{ "username": "Mark" }
		""";

		// Simulate a HTTP call to update a user's profile
		assertThat(mockMvcTester.put().uri("/api/users/{id}", user1.getId())
				.with(authentication(new UsernamePasswordAuthenticationToken(user1.getId(), null, List.of())))
				.contentType(MediaType.APPLICATION_JSON)
				.content(dto))
		        .hasStatus(HttpStatus.CONFLICT)
		        .bodyJson()
		        .hasPathSatisfying("$.error", value -> assertThat(value).isIn("Username already exists"));
	}
	
	@Test
	void givenWrongAccountType_whenUpdatingUser_thenReturnError() {
		String dto = String.format("""
			{ "accountType": "%s" }
		""", "UNKNOWN_ACCOUNT_TYPE");

		// Simulate a HTTP call to update a user's profile
		assertThat(mockMvcTester.put().uri("/api/users/{id}", user1.getId())
				.with(authentication(new UsernamePasswordAuthenticationToken(user1.getId(), null, List.of())))
				.contentType(MediaType.APPLICATION_JSON)
				.content(dto))
		        .hasStatus(HttpStatus.BAD_REQUEST)
		        .bodyJson()
		        .hasPathSatisfying("$.error", value -> assertThat(value).isEqualTo("Invalid enum value"));
	}
	
	@Test
	void givenAnotherUserId_whenUpdatingUser_thenReturnError() {
		String dto = String.format("""
			{ "bio": "A new bio" }
		""");

		// Simulate a HTTP call to update a user's profile
		assertThat(mockMvcTester.put().uri("/api/users/{id}", user3.getId())
				.with(authentication(new UsernamePasswordAuthenticationToken(user1.getId(), null, List.of())))
				.contentType(MediaType.APPLICATION_JSON)
				.content(dto))
		        .hasStatus(HttpStatus.UNAUTHORIZED);
	}
	
	// -------------------------------------------------------------
	// Change user password
	// -------------------------------------------------------------
	@Test
	void givenNewPassword_whenUpdatingPassword_thenSuccess() {
		String dto = """
			{ "oldPassword": "password", "newPassword": "new-password" }
		""";

		// Simulate a HTTP call to change a user's password
		assertThat(mockMvcTester.put().uri("/api/users/{id}/password", user1.getId())
				.with(authentication(new UsernamePasswordAuthenticationToken(user1.getId(), null, List.of())))
				.contentType(MediaType.APPLICATION_JSON)
				.content(dto))
		        .hasStatus(HttpStatus.NO_CONTENT);
	}
	
	@Test
	void givenInvalidNewPassword_whenUpdatingPassword_thenReturnError() {
		String dto = """
			{ "oldPassword": "password", "newPassword": "2small" }
		""";

		// Simulate a HTTP call to change a user's password
		assertThat(mockMvcTester.put().uri("/api/users/{id}/password", user1.getId())
				.with(authentication(new UsernamePasswordAuthenticationToken(user1.getId(), null, List.of())))
				.contentType(MediaType.APPLICATION_JSON)
				.content(dto))
		        .hasStatus(HttpStatus.BAD_REQUEST)
		        .bodyJson()
		        .hasPathSatisfying("$.newPassword", value -> assertThat(value).isEqualTo("size must be between 8 and 100"));
	}
	
	@Test
	void givenAnotherUserId_whenUpdatingPassword_thenReturnError() {
		String dto = """
			{ "oldPassword": "password", "newPassword": "a-new-password" }
		""";

		// Simulate a HTTP call to change a user's password
		assertThat(mockMvcTester.put().uri("/api/users/{id}/password", user3.getId())
				.with(authentication(new UsernamePasswordAuthenticationToken(user1.getId(), null, List.of())))
				.contentType(MediaType.APPLICATION_JSON)
				.content(dto))
		        .hasStatus(HttpStatus.UNAUTHORIZED);
	}
	
	@Test
	void givenWrongOldPassword_whenUpdatingPassword_thenReturnError() {
		String dto = """
			{ "oldPassword": "wrong-password", "newPassword": "new-password" }
		""";

		// Simulate a HTTP call to change a user's password
		assertThat(mockMvcTester.put().uri("/api/users/{id}/password", user1.getId())
				.with(authentication(new UsernamePasswordAuthenticationToken(user1.getId(), null, List.of())))
				.contentType(MediaType.APPLICATION_JSON)
				.content(dto))
		        .hasStatus(HttpStatus.UNAUTHORIZED)
		        .bodyJson()
		        .hasPathSatisfying("$.error", value -> assertThat(value).isEqualTo("Invalid credentials"));
	}
	
	// -------------------------------------------------------------
	// Follow
	// -------------------------------------------------------------
	@Test
	void givenUserId_whenFollow_thenSuccess() {
		String dto = String.format("""
			{ "followedId": "%s" }
		""", user2.getId());
		
		// Simulate a HTTP call to follow a new user
		assertThat(mockMvcTester.post().uri("/api/users/follow")
				.with(authentication(new UsernamePasswordAuthenticationToken(user1.getId(), null, List.of())))
				.contentType(MediaType.APPLICATION_JSON)
				.content(dto))
		        .hasStatus(HttpStatus.NO_CONTENT);
	}
	
	@Test
	void givenMyOwnUserId_whenFollow_thenReturnError() {
		String dto = String.format("""
			{ "followedId": "%s" }
		""", user1.getId());
		
		// Simulate a HTTP call to follow a new user
		assertThat(mockMvcTester.post().uri("/api/users/follow")
				.with(authentication(new UsernamePasswordAuthenticationToken(user1.getId(), null, List.of())))
				.contentType(MediaType.APPLICATION_JSON)
				.content(dto))
		        .hasStatus(HttpStatus.BAD_REQUEST)
		        .bodyJson()
		        .hasPathSatisfying("$.error", value -> assertThat(value).isEqualTo("Cannot follow yourself"));
	}
	
	@Test
	void givenAlreadyFollowingUserId_whenFollow_thenReturnError() {
		String dto = String.format("""
			{ "followedId": "%s" }
		""", user2.getId());
		
		// Simulate a HTTP call to follow a new user
		assertThat(mockMvcTester.post().uri("/api/users/follow")
				.with(authentication(new UsernamePasswordAuthenticationToken(user4.getId(), null, List.of())))
				.contentType(MediaType.APPLICATION_JSON)
				.content(dto))
		        .hasStatus(HttpStatus.CONFLICT)
		        .bodyJson()
		        .hasPathSatisfying("$.error", value -> assertThat(value).isEqualTo("Already following this user"));
	}
	
	@Test
	void givenUnknownFollowedId_whenFollow_thenReturnError() {
		String dto = String.format("""
			{ "followedId": "%s" }
		""", UUID.randomUUID());
		
		// Simulate a HTTP call to follow a new user
		assertThat(mockMvcTester.post().uri("/api/users/follow")
				.with(authentication(new UsernamePasswordAuthenticationToken(user2.getId(), null, List.of())))
				.contentType(MediaType.APPLICATION_JSON)
				.content(dto))
		        .hasStatus(HttpStatus.NOT_FOUND)
		        .bodyJson()
		        .hasPathSatisfying("$.error", value -> assertThat(value).isEqualTo("Followed user not found"));
	}
	
	@Test
	void givenUnknownFollowerId_whenFollow_thenReturnError() {
		String dto = String.format("""
			{ "followedId": "%s" }
		""", user2.getId());
		
		// Simulate a HTTP call to follow a new user
		assertThat(mockMvcTester.post().uri("/api/users/follow")
				.with(authentication(new UsernamePasswordAuthenticationToken(UUID.randomUUID(), null, List.of())))
				.contentType(MediaType.APPLICATION_JSON)
				.content(dto))
		        .hasStatus(HttpStatus.NOT_FOUND)
		        .bodyJson()
		        .hasPathSatisfying("$.error", value -> assertThat(value).isEqualTo("Follower user not found"));
	}

	// -------------------------------------------------------------
	// Un-follow
	// -------------------------------------------------------------
	@Test
	void givenFollowedUserId_whenUnfollow_thenSuccess() {
		String dto = String.format("""
			{ "followedId": "%s" }
		""", user2.getId());
		
		// Simulate a HTTP call to un-follow a user
		assertThat(mockMvcTester.post().uri("/api/users/unfollow")
				.with(authentication(new UsernamePasswordAuthenticationToken(user4.getId(), null, List.of())))
				.contentType(MediaType.APPLICATION_JSON)
				.content(dto))
		        .hasStatus(HttpStatus.NO_CONTENT);
	}
	
	@Test
	void givenNotFollowedUserId_whenUnfollow_thenReturnError() {
		String dto = String.format("""
			{ "followedId": "%s" }
		""", user1.getId());
		
		// Simulate a HTTP call to un-follow a user
		assertThat(mockMvcTester.post().uri("/api/users/unfollow")
				.with(authentication(new UsernamePasswordAuthenticationToken(user4.getId(), null, List.of())))
				.contentType(MediaType.APPLICATION_JSON)
				.content(dto))
		        .hasStatus(HttpStatus.BAD_REQUEST)
		        .bodyJson()
		        .hasPathSatisfying("$.error", value -> assertThat(value).isEqualTo("Not following this user"));
	}
	
	@Test
	void givenMyOwnUserId_whenUnfollow_thenReturnError() {
		String dto = String.format("""
			{ "followedId": "%s" }
		""", user4.getId());
		
		// Simulate a HTTP call to un-follow a user
		assertThat(mockMvcTester.post().uri("/api/users/unfollow")
				.with(authentication(new UsernamePasswordAuthenticationToken(user4.getId(), null, List.of())))
				.contentType(MediaType.APPLICATION_JSON)
				.content(dto))
		        .hasStatus(HttpStatus.BAD_REQUEST)
		        .bodyJson()
		        .hasPathSatisfying("$.error", value -> assertThat(value).isEqualTo("Cannot unfollow yourself"));
	}
	
	@Test
	void givenUnknownFollowedId_whenUnfollow_thenReturnError() {
		String dto = String.format("""
			{ "followedId": "%s" }
		""", UUID.randomUUID());
		
		// Simulate a HTTP call to un-follow a new user
		assertThat(mockMvcTester.post().uri("/api/users/unfollow")
				.with(authentication(new UsernamePasswordAuthenticationToken(user2.getId(), null, List.of())))
				.contentType(MediaType.APPLICATION_JSON)
				.content(dto))
		        .hasStatus(HttpStatus.NOT_FOUND)
		        .bodyJson()
		        .hasPathSatisfying("$.error", value -> assertThat(value).isEqualTo("Followed user not found"));
	}
	
	@Test
	void givenUnknownFollowerId_whenUnfollow_thenReturnError() {
		String dto = String.format("""
			{ "followedId": "%s" }
		""", user2.getId());
		
		// Simulate a HTTP call to un-follow a new user
		assertThat(mockMvcTester.post().uri("/api/users/unfollow")
				.with(authentication(new UsernamePasswordAuthenticationToken(UUID.randomUUID(), null, List.of())))
				.contentType(MediaType.APPLICATION_JSON)
				.content(dto))
		        .hasStatus(HttpStatus.NOT_FOUND)
		        .bodyJson()
		        .hasPathSatisfying("$.error", value -> assertThat(value).isEqualTo("Follower user not found"));
	}
	
	// -------------------------------------------------------------
	// Get followers / following user(s)
	// -------------------------------------------------------------
	@Test
	void givenFollowedId_whenGetFollowers_thenReturnListUsers() {
		// Simulate a HTTP call to fetch a user's list of followers
		assertThat(mockMvcTester.get().uri("/api/users/{id}/followers", user2.getId()))
		        .hasStatus(HttpStatus.OK)
		        .bodyJson()
		        .hasPathSatisfying("$.length()", value -> assertThat(value).isEqualTo(3))
				.hasPathSatisfying("$", value -> assertThat(value).asArray()
					.extracting("id")
					.containsExactlyInAnyOrder(user3.getId().toString(), user4.getId().toString(), user5.getId().toString()));
	}
	
	@Test
	void givenNotFollowedId_whenGetFollowers_thenReturnEmptyList() {
		// Simulate a HTTP call to fetch a user's list of followers
		assertThat(mockMvcTester.get().uri("/api/users/{id}/followers", user1.getId()))
		        .hasStatus(HttpStatus.OK)
		        .bodyJson()
				.hasPathSatisfying("$.length()", value -> assertThat(value).isEqualTo(0));
	}
	
	@Test
	void givenUnknownFollowedId_whenGetFollowers_thenReturnError() {
		// Simulate a HTTP call to fetch a user's list of followers
		assertThat(mockMvcTester.get().uri("/api/users/{id}/followers", UUID.randomUUID()))
		        .hasStatus(HttpStatus.NOT_FOUND)
		        .bodyJson()
		        .hasPathSatisfying("$.error", value -> assertThat(value).isEqualTo("User not found"));
	}
	
	@Test
	void givenFollowerId_whenGetFollowing_thenReturnListUsers() {
		// Simulate a HTTP call to fetch a user's list of followers
		assertThat(mockMvcTester.get().uri("/api/users/{id}/following", user4.getId()))
		        .hasStatus(HttpStatus.OK)
		        .bodyJson()
		        .hasPathSatisfying("$.length()", value -> assertThat(value).isEqualTo(2))
				.hasPathSatisfying("$", value -> assertThat(value).asArray()
					.extracting("id")
					.containsExactlyInAnyOrder(user2.getId().toString(), user3.getId().toString()));
	}
	
	@Test
	void givenNotFollowerId_whenGetFollowing_thenReturnEmptyList() {
		// Simulate a HTTP call to fetch a user's list of followers
		assertThat(mockMvcTester.get().uri("/api/users/{id}/following", user2.getId()))
		        .hasStatus(HttpStatus.OK)
		        .bodyJson()
		        .hasPathSatisfying("$.length()", value -> assertThat(value).isEqualTo(0));
	}
	
	@Test
	void givenUnknownFollowerId_whenGetFollowing_thenReturnError() {
		// Simulate a HTTP call to fetch a user's list of followers
		assertThat(mockMvcTester.get().uri("/api/users/{id}/following", UUID.randomUUID()))
		        .hasStatus(HttpStatus.NOT_FOUND)
		        .bodyJson()
		        .hasPathSatisfying("$.error", value -> assertThat(value).isEqualTo("User not found"));
	}
	
}
