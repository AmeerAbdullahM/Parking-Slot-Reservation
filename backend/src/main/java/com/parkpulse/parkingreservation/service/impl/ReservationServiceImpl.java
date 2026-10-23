package com.parkpulse.parkingreservation.service.impl;

import com.parkpulse.parkingreservation.dto.CheckInCheckOutResponse;
import com.parkpulse.parkingreservation.dto.DashboardStatsResponse;
import com.parkpulse.parkingreservation.dto.ReservationRequest;
import com.parkpulse.parkingreservation.dto.ReservationResponse;
import com.parkpulse.parkingreservation.exception.BadRequestException;
import com.parkpulse.parkingreservation.exception.ResourceNotFoundException;
import com.parkpulse.parkingreservation.exception.SlotConflictException;
import com.parkpulse.parkingreservation.exception.UnauthorizedException;
import com.parkpulse.parkingreservation.model.*;
import com.parkpulse.parkingreservation.repository.*;
import com.parkpulse.parkingreservation.service.OverstayPenaltyService;
import com.parkpulse.parkingreservation.service.ReservationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final ParkingSlotRepository parkingSlotRepository;
    private final ParkingLotRepository parkingLotRepository;
    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;
    private final OverstayPenaltyService overstayPenaltyService;

    public ReservationServiceImpl(ReservationRepository reservationRepository,
                                  ParkingSlotRepository parkingSlotRepository,
                                  ParkingLotRepository parkingLotRepository,
                                  UserRepository userRepository,
                                  VehicleRepository vehicleRepository,
                                  OverstayPenaltyService overstayPenaltyService) {
        this.reservationRepository = reservationRepository;
        this.parkingSlotRepository = parkingSlotRepository;
        this.parkingLotRepository = parkingLotRepository;
        this.userRepository = userRepository;
        this.vehicleRepository = vehicleRepository;
        this.overstayPenaltyService = overstayPenaltyService;
    }

    @Override
    @Transactional
    public ReservationResponse createReservation(ReservationRequest request, Long userId) {
        if (request.getStartTime().isAfter(request.getEndTime()) || request.getStartTime().isEqual(request.getEndTime())) {
            throw new BadRequestException("End time must be strictly after start time");
        }

        long durationMinutes = Duration.between(request.getStartTime(), request.getEndTime()).toMinutes();
        if (durationMinutes < 30) {
            throw new BadRequestException("Minimum reservation duration is 30 minutes");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        ParkingSlot slot = parkingSlotRepository.findById(request.getSlotId())
                .orElseThrow(() -> new ResourceNotFoundException("Parking Slot not found with ID: " + request.getSlotId()));

        if (slot.getStatus() == SlotStatus.MAINTENANCE) {
            throw new BadRequestException("Selected slot is currently undergoing maintenance and cannot be reserved");
        }

        Vehicle vehicle = vehicleRepository.findById(request.getVehicleId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with ID: " + request.getVehicleId()));

        if (!vehicle.getUser().getId().equals(userId) && user.getRole() == Role.ROLE_CUSTOMER) {
            throw new UnauthorizedException("You can only reserve with a vehicle registered under your account");
        }

        // Overlap Collision Detection Query
        List<Reservation> conflicts = reservationRepository.findOverlappingReservations(
                slot.getId(), request.getStartTime(), request.getEndTime()
        );

        if (!conflicts.isEmpty()) {
            throw new SlotConflictException(String.format(
                    "Slot %s is already reserved for the requested window (%s to %s). Please select another slot or time range.",
                    slot.getSlotNumber(), request.getStartTime(), request.getEndTime()
            ));
        }

        // Calculate base fee: ceiling of hours * slot's hourly rate
        long billableHours = (long) Math.ceil((double) durationMinutes / 60.0);
        BigDecimal baseFee = slot.getBaseHourlyRate().multiply(BigDecimal.valueOf(billableHours)).setScale(2, RoundingMode.HALF_UP);

        String reservationCode = "RES-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Reservation reservation = new Reservation(
                reservationCode,
                request.getStartTime(),
                request.getEndTime(),
                baseFee,
                user,
                slot,
                vehicle
        );

        Reservation saved = reservationRepository.save(reservation);
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public CheckInCheckOutResponse checkIn(Long reservationId, Long userId, boolean isStaff) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found with ID: " + reservationId));

        if (!isStaff && !reservation.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You do not have permission to check in for this reservation");
        }

        if (reservation.getStatus() != ReservationStatus.CONFIRMED) {
            throw new BadRequestException("Cannot check in. Current reservation status is: " + reservation.getStatus());
        }

        LocalDateTime now = LocalDateTime.now();
        reservation.setActualCheckIn(now);
        reservation.setStatus(ReservationStatus.CHECKED_IN);

        // Update physical slot status
        ParkingSlot slot = reservation.getSlot();
        slot.setStatus(SlotStatus.OCCUPIED);
        parkingSlotRepository.save(slot);

        reservationRepository.save(reservation);

        return new CheckInCheckOutResponse(
                reservation.getReservationCode(),
                reservation.getStatus(),
                now,
                0L,
                BigDecimal.ZERO,
                reservation.getTotalAmount(),
                "Vehicle checked in successfully. Welcome to " + slot.getParkingLot().getName() + "!"
        );
    }

    @Override
    @Transactional
    public CheckInCheckOutResponse checkOut(Long reservationId, Long userId, boolean isStaff) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found with ID: " + reservationId));

        if (!isStaff && !reservation.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You do not have permission to check out for this reservation");
        }

        if (reservation.getStatus() != ReservationStatus.CHECKED_IN) {
            throw new BadRequestException("Cannot check out. Vehicle must be CHECKED_IN first. Current status: " + reservation.getStatus());
        }

        LocalDateTime now = LocalDateTime.now();
        reservation.setActualCheckOut(now);

        // Free up slot back to available
        ParkingSlot slot = reservation.getSlot();
        slot.setStatus(SlotStatus.AVAILABLE);
        parkingSlotRepository.save(slot);

        // Calculate overstay penalty via Bonus Engine
        OverstayPenaltyService.PenaltyResult penaltyResult = overstayPenaltyService.calculatePenalty(reservation, now);

        reservation.setPenaltyFee(penaltyResult.getPenaltyFee());
        reservation.setTotalAmount(penaltyResult.getTotalAmount());
        reservation.setOverstayMinutes(penaltyResult.getOverstayMinutes());

        if (penaltyResult.isOverstayed()) {
            reservation.setStatus(ReservationStatus.OVERSTAYED);
        } else {
            reservation.setStatus(ReservationStatus.COMPLETED);
        }

        reservationRepository.save(reservation);

        return new CheckInCheckOutResponse(
                reservation.getReservationCode(),
                reservation.getStatus(),
                now,
                penaltyResult.getOverstayMinutes(),
                penaltyResult.getPenaltyFee(),
                penaltyResult.getTotalAmount(),
                penaltyResult.getMessage()
        );
    }

    @Override
    @Transactional
    public ReservationResponse cancelReservation(Long reservationId, Long userId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found with ID: " + reservationId));

        if (!reservation.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You can only cancel your own reservations");
        }

        if (reservation.getStatus() != ReservationStatus.CONFIRMED) {
            throw new BadRequestException("Only CONFIRMED bookings can be cancelled. Current status: " + reservation.getStatus());
        }

        reservation.setStatus(ReservationStatus.CANCELLED);
        Reservation saved = reservationRepository.save(reservation);
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ReservationResponse getReservationById(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found with ID: " + id));
        return mapToResponse(reservation);
    }

    @Override
    @Transactional(readOnly = true)
    public ReservationResponse getReservationByCode(String code) {
        Reservation reservation = reservationRepository.findByReservationCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found with code: " + code));
        return mapToResponse(reservation);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReservationResponse> getUserReservations(Long userId, Pageable pageable) {
        return reservationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReservationResponse> getLotReservations(Long lotId, Pageable pageable) {
        return reservationRepository.findBySlotParkingLotIdOrderByCreatedAtDesc(lotId, pageable)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReservationResponse> searchReservations(Long lotId, ReservationStatus status, String search, Pageable pageable) {
        String query = (search != null && !search.trim().isEmpty()) ? search.trim() : null;
        return reservationRepository.searchReservations(lotId, status, query, pageable)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardStatsResponse getDashboardStats(Long managerId) {
        DashboardStatsResponse stats = new DashboardStatsResponse();

        if (managerId != null) {
            List<ParkingLot> managedLots = parkingLotRepository.findByManagerId(managerId);
            stats.setTotalLots((long) managedLots.size());

            long totalSlots = 0;
            long availableSlots = 0;
            long occupiedSlots = 0;

            for (ParkingLot lot : managedLots) {
                totalSlots += parkingSlotRepository.countByParkingLotId(lot.getId());
                availableSlots += parkingSlotRepository.countByParkingLotIdAndStatus(lot.getId(), SlotStatus.AVAILABLE);
                occupiedSlots += parkingSlotRepository.countByParkingLotIdAndStatus(lot.getId(), SlotStatus.OCCUPIED);
            }

            stats.setTotalSlots(totalSlots);
            stats.setAvailableSlots(availableSlots);
            stats.setOccupiedSlots(occupiedSlots);
        } else {
            stats.setTotalLots(parkingLotRepository.count());
            stats.setTotalSlots(parkingSlotRepository.count());
            stats.setAvailableSlots(parkingSlotRepository.countByParkingLotIdAndStatus(null, SlotStatus.AVAILABLE));
            stats.setOccupiedSlots(parkingSlotRepository.countByParkingLotIdAndStatus(null, SlotStatus.OCCUPIED));
            stats.setTotalManagers((long) userRepository.findByRole(Role.ROLE_MANAGER).size());
            stats.setTotalCustomers((long) userRepository.findByRole(Role.ROLE_CUSTOMER).size());
        }

        List<Reservation> allReservations = reservationRepository.findAll();
        stats.setTotalReservations((long) allReservations.size());

        long activeCount = allReservations.stream()
                .filter(r -> r.getStatus() == ReservationStatus.CONFIRMED || r.getStatus() == ReservationStatus.CHECKED_IN)
                .count();
        stats.setActiveReservations(activeCount);

        BigDecimal totalRevenue = allReservations.stream()
                .filter(r -> r.getStatus() == ReservationStatus.COMPLETED || r.getStatus() == ReservationStatus.OVERSTAYED)
                .map(Reservation::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalPenalties = allReservations.stream()
                .map(Reservation::getPenaltyFee)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        stats.setTotalRevenue(totalRevenue.setScale(2, RoundingMode.HALF_UP));
        stats.setTotalPenalties(totalPenalties.setScale(2, RoundingMode.HALF_UP));

        return stats;
    }

    private ReservationResponse mapToResponse(Reservation r) {
        ReservationResponse dto = new ReservationResponse();
        dto.setId(r.getId());
        dto.setReservationCode(r.getReservationCode());
        dto.setStartTime(r.getStartTime());
        dto.setEndTime(r.getEndTime());
        dto.setActualCheckIn(r.getActualCheckIn());
        dto.setActualCheckOut(r.getActualCheckOut());
        dto.setBaseFee(r.getBaseFee());
        dto.setPenaltyFee(r.getPenaltyFee());
        dto.setTotalAmount(r.getTotalAmount());
        dto.setOverstayMinutes(r.getOverstayMinutes());
        dto.setStatus(r.getStatus());
        dto.setCreatedAt(r.getCreatedAt());

        if (r.getUser() != null) {
            dto.setUserId(r.getUser().getId());
            dto.setUserFullName(r.getUser().getFullName());
            dto.setUserEmail(r.getUser().getEmail());
            dto.setUserPhone(r.getUser().getPhone());
        }

        if (r.getSlot() != null) {
            ParkingSlot slot = r.getSlot();
            dto.setSlotId(slot.getId());
            dto.setSlotNumber(slot.getSlotNumber());
            dto.setFloorNumber(slot.getFloorNumber());
            dto.setSlotType(slot.getSlotType());
            dto.setBaseHourlyRate(slot.getBaseHourlyRate());

            if (slot.getParkingLot() != null) {
                dto.setLotId(slot.getParkingLot().getId());
                dto.setLotName(slot.getParkingLot().getName());
                dto.setLotAddress(slot.getParkingLot().getAddress());
            }
        }

        if (r.getVehicle() != null) {
            dto.setVehicleId(r.getVehicle().getId());
            dto.setLicensePlate(r.getVehicle().getLicensePlate());
            dto.setVehicleType(r.getVehicle().getVehicleType());
            dto.setVehicleModel(r.getVehicle().getModel());
        }

        return dto;
    }
}
