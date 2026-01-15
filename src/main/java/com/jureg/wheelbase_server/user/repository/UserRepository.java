package com.jureg.wheelbase_server.user.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jureg.wheelbase_server.user.model.User;

// Extending the JpaRepository allows for CRUD operations, pagination and utilities functions
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
	
	Optional<User> findByUsernameIgnoreCase(String username);
	Optional<User> findByEmailIgnoreCase(String email);
	Optional<User> findByUsernameIgnoreCaseOrEmailIgnoreCase(String username, String email);
	List<User> findByUsernameContainingIgnoreCase(String username);
	
	boolean existsByUsernameIgnoreCase(String username);
	boolean existsByEmailIgnoreCase(String email);
}
