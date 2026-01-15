package com.jureg.wheelbase_server.vehicle_brand.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jureg.wheelbase_server.vehicle_brand.model.VehicleBrand;

// Extending the JpaRepository allows for CRUD operations, pagination and utilities functions
@Repository
public interface VehicleBrandRepository extends JpaRepository<VehicleBrand, UUID> {

}
