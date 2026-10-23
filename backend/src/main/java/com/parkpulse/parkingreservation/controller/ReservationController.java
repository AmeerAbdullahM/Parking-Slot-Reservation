package com.parkpulse.parkingreservation.controller;

import com.parkpulse.parkingreservation.dto.CheckInCheckOutResponse;
import com.parkpulse.parkingreservation.dto.ReservationRequest;
import com.parkpulse.parkingreservation.dto.ReservationResponse;
import com.parkpulse.parkingreservation.model.ReservationStatus;
import com.parkpulse.parkingreservation.model.Role;
import com.parkpulse.parkingreservation.model.User;
import com.parkpulse.parkingreservation.service.AuthService;
import com.parkpulse.parkingreservation.service.ReservationService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;
    private final AuthService authService;

    public ReservationController(ReservationService reservationService, AuthService authService) {
        this.reservationService = reservationService;
        this.authService = authService;
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> createReservation(@Valid @RequestBody ReservationRequest request) {
        User currentUser = authService.getCurrentAuthenticatedUser();
        ReservationResponse response = reservationService.createReservation(request, currentUser.getId());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/my")
    public ResponseEntity<Page<ReservationResponse>> getMyReservations(
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        User currentUser = authService.getCurrentAuthenticatedUser();
        return ResponseEntity.ok(reservationService.getUserReservations(currentUser.getId(), pageable));
    }

    @GetMapping("/lot/{lotId}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<Page<ReservationResponse>> getLotReservations(
            @PathVariable Long lotId,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(reservationService.getLotReservations(lotId, pageable));
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<Page<ReservationResponse>> searchReservations(
            @RequestParam(required = false) Long lotId,
            @RequestParam(required = false) ReservationStatus status,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(reservationService.searchReservations(lotId, status, search, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReservationResponse> getReservationById(@PathVariable Long id) {
        return ResponseEntity.ok(reservationService.getReservationById(id));
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<ReservationResponse> getReservationByCode(@PathVariable String code) {
        return ResponseEntity.ok(reservationService.getReservationByCode(code));
    }

    @PostMapping("/{id}/check-in")
    public ResponseEntity<CheckInCheckOutResponse> checkIn(@PathVariable Long id) {
        User currentUser = authService.getCurrentAuthenticatedUser();
        boolean isStaff = currentUser.getRole() == Role.ROLE_ADMIN || currentUser.getRole() == Role.ROLE_MANAGER;
        return ResponseEntity.ok(reservationService.checkIn(id, currentUser.getId(), isStaff));
    }

    @PostMapping("/{id}/check-out")
    public ResponseEntity<CheckInCheckOutResponse> checkOut(@PathVariable Long id) {
        User currentUser = authService.getCurrentAuthenticatedUser();
        boolean isStaff = currentUser.getRole() == Role.ROLE_ADMIN || currentUser.getRole() == Role.ROLE_MANAGER;
        return ResponseEntity.ok(reservationService.checkOut(id, currentUser.getId(), isStaff));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ReservationResponse> cancelReservation(@PathVariable Long id) {
        User currentUser = authService.getCurrentAuthenticatedUser();
        return ResponseEntity.ok(reservationService.cancelReservation(id, currentUser.getId()));
    }
}
