package com.jureg.wheelbase_server.follow.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jureg.wheelbase_server.follow.model.Follow;
import com.jureg.wheelbase_server.follow.model.FollowId;
import com.jureg.wheelbase_server.user.model.User;

// Extending the JpaRepository allows for CRUD operations, pagination and utilities functions
@Repository
public interface FollowRepository extends JpaRepository<Follow, FollowId> {

	boolean existsByFollowerIdAndFollowedId(UUID followerId, UUID followedId);
	
	List<Follow> findByFollower(User user);
	List<Follow> findByFollowed(User user);
	
	void deleteByFollowerIdAndFollowedId(UUID followerId, UUID followedId);
}
