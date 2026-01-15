package com.jureg.wheelbase_server.user.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.junit.jupiter.Container;

import com.jureg.wheelbase_server.community_type.model.CommunityType;
import com.jureg.wheelbase_server.user.model.User;

@DataJpaTest
@Testcontainers
class UserRepositoryTest {
	
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
	private UserRepository userRepository;
	
	// -------------------------------------------------------------
	// Find users
	// -------------------------------------------------------------
	@Test
	void givenUsername_whenFindByUsername_thenReturnUser() {
		 userRepository.saveAndFlush(createUser("John"));
		
		Optional<User> foundUser = userRepository.findByUsernameIgnoreCase("john");
		
		assertThat(foundUser).isPresent();
		assertThat(foundUser.get().getUsername()).isEqualTo("John");
	}
	
	@Test
	void givenUnknownUsername_whenFindByUsername_thenReturnEmpty() {
		 userRepository.saveAndFlush(createUser("John"));
		
		Optional<User> foundUser = userRepository.findByUsernameIgnoreCase("Alice");
		
		assertThat(foundUser).isEmpty();
	}
	
	@Test
	void givenEmail_whenFindByEmail_thenReturnUser() {
		 userRepository.saveAndFlush(createUser("John"));
		
		Optional<User> foundUser = userRepository.findByEmailIgnoreCase("john@TEST.com");
		
		assertThat(foundUser).isPresent();
		assertThat(foundUser.get().getUsername()).isEqualTo("John");
	}
	
	@Test
	void givenUnknownEmail_whenFindByEmail_thenReturnEmpty() {
		 userRepository.saveAndFlush(createUser("John"));
		
		Optional<User> foundUser = userRepository.findByEmailIgnoreCase("Alice@test.com");
		
		assertThat(foundUser).isEmpty();
	}
	
	@Test
	void givenUsername_whenFindByUsernameOrEmail_thenReturnUser() {
		userRepository.saveAndFlush(createUser("John"));
		
		Optional<User> foundUser = userRepository.findByUsernameIgnoreCaseOrEmailIgnoreCase("John", "Alice@test.com");
		
		assertThat(foundUser).isPresent();
		assertThat(foundUser.get().getUsername()).isEqualTo("John");
	}
	
	@Test
	void givenUnknownUsername_whenFindByUsernameOrEmail_thenReturnEmpty() {
		userRepository.saveAndFlush(createUser("John"));
		
		Optional<User> foundUser = userRepository.findByUsernameIgnoreCaseOrEmailIgnoreCase("Alice", "Alice@test.com");
		
		assertThat(foundUser).isEmpty();
	}

	@Test
	void givenEmail_whenFindByUsernameOrEmail_thenReturnUser() {
		userRepository.saveAndFlush(createUser("John"));
		
		Optional<User> foundUser = userRepository.findByUsernameIgnoreCaseOrEmailIgnoreCase("Alice", "john@test.com");
		
		assertThat(foundUser).isPresent();
		assertThat(foundUser.get().getUsername()).isEqualTo("John");
	}
	
	@Test
	void givenUnknownEmail_whenFindByUsernameOrEmail_thenReturnEmpty() {
		userRepository.saveAndFlush(createUser("John"));
		
		Optional<User> foundUser = userRepository.findByUsernameIgnoreCaseOrEmailIgnoreCase("Alice", "Alice@test.com");
		
		assertThat(foundUser).isEmpty();
	}
	
	@Test
	void givenPartUsername_whenFindByUsernameContaining_thenReturnListUsers() {
		userRepository.saveAndFlush(createUser("John"));
		List<User> foundUser = userRepository.findByUsernameContainingIgnoreCase("john");
		assertThat(foundUser).hasSize(1);
		
		userRepository.saveAndFlush(createUser("Johny"));
		foundUser = userRepository.findByUsernameContainingIgnoreCase("john");
		assertThat(foundUser).hasSize(2);
		
		userRepository.saveAndFlush(createUser("Johnny"));
		foundUser = userRepository.findByUsernameContainingIgnoreCase("john");
		assertThat(foundUser).hasSize(3);
		
		userRepository.saveAndFlush(createUser("Alice"));
		foundUser = userRepository.findByUsernameContainingIgnoreCase("john");
		assertThat(foundUser).hasSize(3);
		
		userRepository.saveAndFlush(createUser("Old_John"));
		foundUser = userRepository.findByUsernameContainingIgnoreCase("john");
		assertThat(foundUser).hasSize(4);
	}
	
	@Test
	void givenNonExistingUsername_whenExistsByUsername_thenReturnFalse() {
		userRepository.saveAndFlush(createUser("John"));
		userRepository.saveAndFlush(createUser("Old_John"));
		userRepository.saveAndFlush(createUser("Frank"));
		
		boolean exists = userRepository.existsByUsernameIgnoreCase("Alice");
		assertThat(exists).isFalse();
		
		exists = userRepository.existsByUsernameIgnoreCase("Johny");
		assertThat(exists).isFalse();
	}
	
	@Test
	void givenExistingUsername_whenExistsByUsername_thenReturnTrue() {
		userRepository.saveAndFlush(createUser("John"));
		userRepository.saveAndFlush(createUser("Old_John"));
		userRepository.saveAndFlush(createUser("Frank"));
		
		boolean exists = userRepository.existsByUsernameIgnoreCase("Frank");
		assertThat(exists).isTrue();
		
		exists = userRepository.existsByUsernameIgnoreCase("old_JOHN");
		assertThat(exists).isTrue();
	}
	
	@Test
	void givenNonExistingEmail_whenExistsByEmail_thenReturnFalse() {
		userRepository.saveAndFlush(createUser("John"));
		userRepository.saveAndFlush(createUser("Old_John"));
		userRepository.saveAndFlush(createUser("Frank"));
		
		boolean exists = userRepository.existsByEmailIgnoreCase("Alice@test.com");
		assertThat(exists).isFalse();
		
		exists = userRepository.existsByEmailIgnoreCase("Johny@test.com");
		assertThat(exists).isFalse();
	}
	
	@Test
	void givenExistingEmail_whenExistsByEmail_thenReturnTrue() {
		userRepository.saveAndFlush(createUser("John"));
		userRepository.saveAndFlush(createUser("Old_John"));
		userRepository.saveAndFlush(createUser("Frank"));
		
		boolean exists = userRepository.existsByEmailIgnoreCase("Frank@test.com");
		assertThat(exists).isTrue();
		
		exists = userRepository.existsByEmailIgnoreCase("JOHN@test.com");
		assertThat(exists).isTrue();
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













