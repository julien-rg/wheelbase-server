package com.jureg.wheelbase_server.post.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.jureg.wheelbase_server.community_type.model.CommunityType;
import com.jureg.wheelbase_server.post_comment.model.PostComment;
import com.jureg.wheelbase_server.post_image.model.PostImage;
import com.jureg.wheelbase_server.post_like.model.PostLike;
import com.jureg.wheelbase_server.user.model.User;
import com.jureg.wheelbase_server.vehicle.model.Vehicle;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "posts")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Post {
	
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;
	
	// A post can have only one author
	@ManyToOne
	@JoinColumn(name = "author_id", nullable = false)
	private User author;
	
	// A post can have only one vehicle
	@ManyToOne
	@JoinColumn(name = "vehicle_id", nullable = false)
	private Vehicle vehicle;
	
	// A post belongs to only one community
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private CommunityType community;
	
	@Column(columnDefinition = "TEXT")
	private String content;
	
	@Builder.Default
	private Instant createdAt = Instant.now();
	
	// A post can have 0 or more images
	@OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
	@Builder.Default
	private List<PostImage> images = new ArrayList<>();
	
	// A post can have 0 or more likes
	@OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
	@Builder.Default
	private List<PostLike> likes = new ArrayList<>();
	
	// A post can have 0 or more comments
	@OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
	@Builder.Default
	private List<PostComment> comments = new ArrayList<>();

}
