package com.jureg.wheelbase_server.post_comment.model;

import java.time.Instant;
import java.util.UUID;

import com.jureg.wheelbase_server.post.model.Post;
import com.jureg.wheelbase_server.user.model.User;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "comments")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostComment {
	
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;
	
	// A comment belongs to one, and only one, post
	@ManyToOne
	@JoinColumn(name = "post_id", nullable = false)
	private Post post;
	
	// A comment is written by one, and only one, user
	@ManyToOne
	@JoinColumn(name = "author_id", nullable = false)
	private User author;

	@Column(columnDefinition = "TEXT", nullable = false)
	private String content;
	
	@Builder.Default
	private Instant createdAt = Instant.now();
}
