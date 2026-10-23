package com.parkpulse.parkingreservation.service;

import com.parkpulse.parkingreservation.dto.CheckInCheckOutResponse;
import com.parkpulse.parkingreservation.dto.ReservationRequest;
import com.parkpulse.parkingreservation.dto.ReservationResponse;
import com.parkpulse.parkingreservation.exception.BadRequestException;
import com.parkpulse.parkingreservation.exception.SlotConflictException;
import com.parkpulse.parkingreservation.model.*;
import com.parkpulse.parkingreservation.repository.*;
import com.parkpulse.parkingreservation.service.impl.ReservationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private ParkingSlotRepository parkingSlotRepository;

    @Mock
    private ParkingLotRepository parkingLotRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private OverstayPenaltyService overstayPenaltyService;

    @InjectMocks
    private ReservationServiceImpl reservationService;

    private User sampleUser;
    private ParkingLot sampleLot;
    private ParkingSlot sampleSlot;
    private Vehicle sampleVehicle;
    private Reservation sampleReservation;

    @BeforeEach
    void setUp() {
        sampleUser = new User("Alice Brown", "alice@example.com", "hash", "+1-555-9000", Role.ROLE_CUSTOMER);
        sampleUser.setId(10L);

        sampleLot = new ParkingLot("Grand Plaza Mall", LocationType.MALL, "123 Main St", 2, 20, null);
        sampleLot.setId(1L);

        sampleSlot = new ParkingSlot("G-01", 1, 1, 1, SlotType.REGULAR, BigDecimal.valueOf(5.00), sampleLot);
        sampleSlot.setId(100L);

        sampleVehicle = new Vehicle("NY-9999", VehicleType.SEDAN, "Honda Accord", sampleUser);
        sampleVehicle.setId(50L);

        sampleReservation = new Reservation("RES-TEST1234", LocalDateTime.now(), LocalDateTime.now().plusHours(2),
                BigDecimal.valueOf(10.00), sampleUser, sampleSlot, sampleVehicle);
        sampleReservation.setId(500L);
    }

    @Test
    @DisplayName("Should successfully create a reservation when slot has no overlapping bookings")
    void testCreateReservation_Success() {
        LocalDateTime start = LocalDateTime.now().plusHours(1);
        LocalDateTime end = LocalDateTime.now().plusHours(3);
        ReservationRequest request = new ReservationRequest(100L, 50L, start, end);

        when(userRepository.findById(10L)).thenReturn(Optional.of(sampleUser));
        when(parkingSlotRepository.findById(100L)).thenReturn(Optional.of(sampleSlot));
        when(vehicleRepository.findById(50L)).thenReturn(Optional.of(sampleVehicle));
        when(reservationRepository.findOverlappingReservations(100L, start, end)).thenReturn(Collections.emptyList());
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(invocation -> {
            Reservation r = invocation.getArgument(0);
            r.setId(999L);
            return r;
        });

        ReservationResponse response = reservationService.createReservation(request, 10L);

        assertNotNull(response);
        assertEquals("G-01", response.getSlotNumber());
        assertEquals("NY-9999", response.getLicensePlate());
        assertEquals(ReservationStatus.CONFIRMED, response.getStatus());
        assertEquals(BigDecimal.valueOf(10.00).setScale(2), response.getBaseFee());
        verify(reservationRepository, times(1)).save(any(Reservation.class));
    }

    @Test
    @DisplayName("Should throw SlotConflictException when requested time overlaps with existing booking")
    void testCreateReservation_SlotConflict_ThrowsSlotConflictException() {
        LocalDateTime start = LocalDateTime.now().plusHours(1);
        LocalDateTime end = LocalDateTime.now().plusHours(3);
        ReservationRequest request = new ReservationRequest(100L, 50L, start, end);

        when(userRepository.findById(10L)).thenReturn(Optional.of(sampleUser));
        when(parkingSlotRepository.findById(100L)).thenReturn(Optional.of(sampleSlot));
        when(vehicleRepository.findById(50L)).thenReturn(Optional.of(sampleVehicle));
        when(reservationRepository.findOverlappingReservations(100L, start, end))
                .thenReturn(List.of(sampleReservation));

        assertThrows(SlotConflictException.class, () -> reservationService.createReservation(request, 10L));
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    @DisplayName("Should successfully check-in vehicle and change slot status to OCCUPIED")
    void testCheckIn_Success_MarksOccupied() {
        when(reservationRepository.findById(500L)).thenReturn(Optional.of(sampleReservation));

        CheckInCheckOutResponse response = reservationService.checkIn(500L, 10L, false);

        assertEquals(ReservationStatus.CHECKED_IN, response.getStatus());
        assertEquals(SlotStatus.OCCUPIED, sampleSlot.getStatus());
        assertNotNull(sampleReservation.getActualCheckIn());
        verify(parkingSlotRepository, times(1)).save(sampleSlot);
        verify(reservationRepository, times(1)).save(sampleReservation);
    }

    @Test
    @DisplayName("Should reject check-in if reservation is not in CONFIRMED status")
    void testCheckIn_InvalidStatus_ThrowsBadRequestException() {
        sampleReservation.setStatus(ReservationStatus.CANCELLED);
        when(reservationRepository.findById(500L)).thenReturn(Optional.of(sampleReservation));

        assertThrows(BadRequestException.class, () -> reservationService.checkIn(500L, 10L, false));
    }
}
