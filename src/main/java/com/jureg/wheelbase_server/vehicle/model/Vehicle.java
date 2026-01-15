package com.jureg.wheelbase_server.vehicle.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.jureg.wheelbase_server.community_type.model.CommunityType;
import com.jureg.wheelbase_server.post.model.Post;
import com.jureg.wheelbase_server.user.model.User;
import com.jureg.wheelbase_server.vehicle_brand.model.VehicleBrand;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "vehicles")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vehicle {
	
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;
	
	// A vehicle can be owned only by one User
	@ManyToOne
	@JoinColumn(name = "owner_id", nullable = false)
	private User owner;
	
	// A vehicle belongs to one, and only one, community
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private CommunityType community;
	
	// A vehicle brand comes from a list, or entered manually by user if not exist
	@ManyToOne
	@JoinColumn(name = "brand_id")
	private VehicleBrand brand;
	@Column(name = "custom_brand")
	private String customBrand;
	
	@Column(nullable = false)
	private String model;
	
	private Integer year;
	
	@Column(columnDefinition = "TEXT")
	private String description;
	
	// A vehicle can be part of 0 or more posts
	@OneToMany(mappedBy = "vehicle", cascade = CascadeType.ALL)
	@Builder.Default
	private List<Post> posts = new ArrayList<>();

}
