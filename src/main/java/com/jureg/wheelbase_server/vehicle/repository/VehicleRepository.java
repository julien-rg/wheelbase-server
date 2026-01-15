package com.jureg.wheelbase_server.vehicle.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jureg.wheelbase_server.vehicle.model.Vehicle;

// Extending the JpaRepository allows for CRUD operations, pagination and utilities functions
@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, UUID> {

}
