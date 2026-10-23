package com.parkpulse.parkingreservation.service;

import com.parkpulse.parkingreservation.dto.CheckInCheckOutResponse;
import com.parkpulse.parkingreservation.dto.DashboardStatsResponse;
import com.parkpulse.parkingreservation.dto.ReservationRequest;
import com.parkpulse.parkingreservation.dto.ReservationResponse;
import com.parkpulse.parkingreservation.model.ReservationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReservationService {
    ReservationResponse createReservation(ReservationRequest request, Long userId);
    CheckInCheckOutResponse checkIn(Long reservationId, Long userId, boolean isStaff);
    CheckInCheckOutResponse checkOut(Long reservationId, Long userId, boolean isStaff);
    ReservationResponse cancelReservation(Long reservationId, Long userId);
    ReservationResponse getReservationById(Long id);
    ReservationResponse getReservationByCode(String code);
    Page<ReservationResponse> getUserReservations(Long userId, Pageable pageable);
    Page<ReservationResponse> getLotReservations(Long lotId, Pageable pageable);
    Page<ReservationResponse> searchReservations(Long lotId, ReservationStatus status, String search, Pageable pageable);
    DashboardStatsResponse getDashboardStats(Long managerId);
}
