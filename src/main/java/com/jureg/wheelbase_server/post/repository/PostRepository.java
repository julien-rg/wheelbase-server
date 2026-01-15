package com.jureg.wheelbase_server.post.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jureg.wheelbase_server.post_like.model.PostLike;
import com.jureg.wheelbase_server.post_like.model.PostLikeId;

// Extending the JpaRepository allows for CRUD operations, pagination and utilities functions
@Repository
public interface PostRepository extends JpaRepository<PostLike, PostLikeId> {

}
