package com.jureg.wheelbase_server.follow.model;

import java.io.Serializable;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// This is the composite key for a Follow
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FollowId implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private UUID follower;
	private UUID followed;
	
}
