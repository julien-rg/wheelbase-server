package com.jureg.wheelbase_server.post_image.model;

import java.util.UUID;

import com.jureg.wheelbase_server.post.model.Post;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "post_images")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostImage {
	
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;
	
	// A comment belongs to one, and only one, post
	@ManyToOne
	@JoinColumn(name = "post_id", nullable = false)
	private Post post;
	
	@Column(columnDefinition = "TEXT", nullable = false)
	private String imageUrl;

}
