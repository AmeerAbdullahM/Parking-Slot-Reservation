package com.parkpulse.parkingreservation.controller;

import com.parkpulse.parkingreservation.dto.VehicleRequest;
import com.parkpulse.parkingreservation.dto.VehicleResponse;
import com.parkpulse.parkingreservation.model.User;
import com.parkpulse.parkingreservation.service.AuthService;
import com.parkpulse.parkingreservation.service.VehicleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
public class VehicleController {

    private final VehicleService vehicleService;
    private final AuthService authService;

    public VehicleController(VehicleService vehicleService, AuthService authService) {
        this.vehicleService = vehicleService;
        this.authService = authService;
    }

    @PostMapping
    public ResponseEntity<VehicleResponse> registerVehicle(@Valid @RequestBody VehicleRequest request) {
        User currentUser = authService.getCurrentAuthenticatedUser();
        VehicleResponse response = vehicleService.registerVehicle(request, currentUser.getId());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/my")
    public ResponseEntity<List<VehicleResponse>> getMyVehicles() {
        User currentUser = authService.getCurrentAuthenticatedUser();
        return ResponseEntity.ok(vehicleService.getUserVehicles(currentUser.getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<VehicleResponse> getVehicleById(@PathVariable Long id) {
        return ResponseEntity.ok(vehicleService.getVehicleById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVehicle(@PathVariable Long id) {
        User currentUser = authService.getCurrentAuthenticatedUser();
        vehicleService.deleteVehicle(id, currentUser.getId());
        return ResponseEntity.noContent().build();
    }
}
