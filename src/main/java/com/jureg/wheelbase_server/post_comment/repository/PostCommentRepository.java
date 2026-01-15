package com.jureg.wheelbase_server.post_comment.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jureg.wheelbase_server.post_comment.model.PostComment;

// Extending the JpaRepository allows for CRUD operations, pagination and utilities functions
@Repository
public interface PostCommentRepository extends JpaRepository<PostComment, UUID> {

}
