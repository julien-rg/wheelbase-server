package com.jureg.wheelbase_server.post_like.model;

import java.io.Serializable;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// This is the composite key for a PostLike
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostLikeId implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private UUID post;
	private UUID user;
}
