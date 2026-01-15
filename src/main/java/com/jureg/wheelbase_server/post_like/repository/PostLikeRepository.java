package com.jureg.wheelbase_server.post_like.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jureg.wheelbase_server.post.model.Post;

// Extending the JpaRepository allows for CRUD operations, pagination and utilities functions
@Repository
public interface PostLikeRepository extends JpaRepository<Post, UUID> {

}
