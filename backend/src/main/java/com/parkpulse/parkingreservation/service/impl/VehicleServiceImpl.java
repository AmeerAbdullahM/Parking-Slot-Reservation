package com.parkpulse.parkingreservation.service.impl;

import com.parkpulse.parkingreservation.dto.VehicleRequest;
import com.parkpulse.parkingreservation.dto.VehicleResponse;
import com.parkpulse.parkingreservation.exception.BadRequestException;
import com.parkpulse.parkingreservation.exception.ResourceNotFoundException;
import com.parkpulse.parkingreservation.exception.UnauthorizedException;
import com.parkpulse.parkingreservation.model.User;
import com.parkpulse.parkingreservation.model.Vehicle;
import com.parkpulse.parkingreservation.repository.UserRepository;
import com.parkpulse.parkingreservation.repository.VehicleRepository;
import com.parkpulse.parkingreservation.service.VehicleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class VehicleServiceImpl implements VehicleService {

    private final VehicleRepository vehicleRepository;
    private final UserRepository userRepository;

    public VehicleServiceImpl(VehicleRepository vehicleRepository, UserRepository userRepository) {
        this.vehicleRepository = vehicleRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public VehicleResponse registerVehicle(VehicleRequest request, Long userId) {
        String plate = request.getLicensePlate().trim().toUpperCase();
        if (vehicleRepository.existsByLicensePlate(plate)) {
            throw new BadRequestException("Vehicle with license plate '" + plate + "' is already registered");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        Vehicle vehicle = new Vehicle(
                plate,
                request.getVehicleType(),
                request.getModel() != null ? request.getModel().trim() : null,
                user
        );

        Vehicle saved = vehicleRepository.save(vehicle);
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehicleResponse> getUserVehicles(Long userId) {
        return vehicleRepository.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public VehicleResponse getVehicleById(Long id) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with ID: " + id));
        return mapToResponse(vehicle);
    }

    @Override
    @Transactional
    public void deleteVehicle(Long id, Long userId) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with ID: " + id));

        if (!vehicle.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You do not have permission to delete this vehicle");
        }

        vehicleRepository.delete(vehicle);
    }

    private VehicleResponse mapToResponse(Vehicle vehicle) {
        return new VehicleResponse(
                vehicle.getId(),
                vehicle.getLicensePlate(),
                vehicle.getVehicleType(),
                vehicle.getModel(),
                vehicle.getUser().getId()
        );
    }
}
