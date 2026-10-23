package com.parkpulse.parkingreservation.service;

import com.parkpulse.parkingreservation.dto.VehicleRequest;
import com.parkpulse.parkingreservation.dto.VehicleResponse;

import java.util.List;

public interface VehicleService {
    VehicleResponse registerVehicle(VehicleRequest request, Long userId);
    List<VehicleResponse> getUserVehicles(Long userId);
    VehicleResponse getVehicleById(Long id);
    void deleteVehicle(Long id, Long userId);
}
