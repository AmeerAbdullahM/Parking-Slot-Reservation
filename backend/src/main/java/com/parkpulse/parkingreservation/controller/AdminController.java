package com.parkpulse.parkingreservation.controller;

import com.parkpulse.parkingreservation.dto.AuthResponse;
import com.parkpulse.parkingreservation.dto.CreateManagerRequest;
import com.parkpulse.parkingreservation.dto.DashboardStatsResponse;
import com.parkpulse.parkingreservation.model.Role;
import com.parkpulse.parkingreservation.model.User;
import com.parkpulse.parkingreservation.repository.UserRepository;
import com.parkpulse.parkingreservation.service.AuthService;
import com.parkpulse.parkingreservation.service.ReservationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AuthService authService;
    private final UserRepository userRepository;
    private final ReservationService reservationService;

    public AdminController(AuthService authService,
                           UserRepository userRepository,
                           ReservationService reservationService) {
        this.authService = authService;
        this.userRepository = userRepository;
        this.reservationService = reservationService;
    }

    @PostMapping("/managers")
    public ResponseEntity<AuthResponse> createManager(@Valid @RequestBody CreateManagerRequest request) {
        AuthResponse response = authService.createManager(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/managers")
    public ResponseEntity<List<User>> getAllManagers() {
        return ResponseEntity.ok(userRepository.findByRole(Role.ROLE_MANAGER));
    }

    @GetMapping("/customers")
    public ResponseEntity<List<User>> getAllCustomers() {
        return ResponseEntity.ok(userRepository.findByRole(Role.ROLE_CUSTOMER));
    }

    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsResponse> getGlobalStats() {
        return ResponseEntity.ok(reservationService.getDashboardStats(null));
    }

    @GetMapping("/manager-stats/{managerId}")
    public ResponseEntity<DashboardStatsResponse> getManagerStats(@PathVariable Long managerId) {
        return ResponseEntity.ok(reservationService.getDashboardStats(managerId));
    }
}
