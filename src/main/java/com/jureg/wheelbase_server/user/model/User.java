package com.jureg.wheelbase_server.user.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jureg.wheelbase_server.community_type.model.CommunityType;
import com.jureg.wheelbase_server.follow.model.Follow;
import com.jureg.wheelbase_server.post.model.Post;
import com.jureg.wheelbase_server.vehicle.model.Vehicle;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "users")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
	
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;
	
	@Column(unique = true, nullable = false)
	private String username;
	
	@Email(message = "Invalid email format")
	@Column(unique = true, nullable = false)
	private String email;
	
	@JsonIgnore
	@Column(nullable = false)
	private String password;
	
	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	@Builder.Default
	private AccountType accountType = AccountType.FOLLOWERS_ONLY;
	
	private String avatarUrl;
	private String bio;
	
	// A user can have 0 or more vehicles
	// Each vehicle has a owner defined by the "owner" field in the Vehicle class
	@OneToMany(mappedBy = "owner", cascade = CascadeType.ALL)
	@Builder.Default
	private List<Vehicle> vehicles = new ArrayList<>();
	
	// A user can have 0 or more posts
	// Each post has a author defined by the "author" field in the Post class
	@OneToMany(mappedBy = "author", cascade = CascadeType.ALL)
	@Builder.Default
	private List<Post> posts = new ArrayList<>();
	
	// A user can decide to be part of multiple communities
	// This create a table "user_communities" that contains the user ID and the community he belongs to
	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "user_communities", joinColumns = @JoinColumn(name = "user_id"))
	@Column(name = "community")
	@Enumerated(EnumType.STRING)
	@Size(min = 1, message = "At least one community is required")
	@Builder.Default
	private Set<CommunityType> communities = new HashSet<>(Set.of(CommunityType.CAR));
	
	// A user can follow 0 or more users
	// This hash contains all the Follow that this users has (as a follower)
	@OneToMany(mappedBy = "follower", cascade = CascadeType.ALL)
	@Builder.Default
	private Set<Follow> following = new HashSet<>();
	
	// A user can be followed by 0 or more users
	// This hash contains all the Follow that this users has (as a followed)
	@OneToMany(mappedBy = "followed", cascade = CascadeType.ALL)
	@Builder.Default
	private Set<Follow> followers = new HashSet<>();

}
