package com.jureg.wheelbase_server.post_like.model;

import java.time.Instant;

import com.jureg.wheelbase_server.post.model.Post;
import com.jureg.wheelbase_server.user.model.User;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// This table contains a composite key (a post ID + a user ID)
// We use this to allow fast requests later on
@Data
@Entity
@Table(name = "post_likes")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(PostLikeId.class)
public class PostLike {
	
	@Id
	@ManyToOne
	@JoinColumn(name = "post_id")
	private Post post;
	
	@Id
	@ManyToOne
	@JoinColumn(name = "user_id")
	private User user;
	
	@Builder.Default
	private Instant createdAt = Instant.now();

}
