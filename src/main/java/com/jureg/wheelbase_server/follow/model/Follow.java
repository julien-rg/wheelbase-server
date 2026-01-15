package com.jureg.wheelbase_server.follow.model;

import java.time.Instant;

import com.jureg.wheelbase_server.user.model.User;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// This table contains a composite key (a user ID + a user ID)
// We use this to allow fast requests later on
@Data
@Entity
@Table(name = "follows")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(FollowId.class)
public class Follow {
	
	@Id
	@ManyToOne
	@JoinColumn(name = "follower_id")
	private User follower;
	
	@Id
	@ManyToOne
	@JoinColumn(name = "followed_id")
	private User followed;
	
	@Builder.Default
	private Instant createdAt = Instant.now();

}
