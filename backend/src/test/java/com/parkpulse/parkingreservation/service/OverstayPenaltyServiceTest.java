package com.parkpulse.parkingreservation.service;

import com.parkpulse.parkingreservation.model.ParkingSlot;
import com.parkpulse.parkingreservation.model.Reservation;
import com.parkpulse.parkingreservation.service.impl.OverstayPenaltyServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class OverstayPenaltyServiceTest {

    @InjectMocks
    private OverstayPenaltyServiceImpl penaltyService;

    private Reservation sampleReservation;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(penaltyService, "gracePeriodMinutes", 15L);
        ReflectionTestUtils.setField(penaltyService, "penaltyMultiplier", 1.5);

        ParkingSlot slot = new ParkingSlot();
        slot.setBaseHourlyRate(BigDecimal.valueOf(10.00));

        sampleReservation = new Reservation();
        sampleReservation.setBaseFee(BigDecimal.valueOf(20.00));
        sampleReservation.setStartTime(LocalDateTime.now().minusHours(2));
        sampleReservation.setEndTime(LocalDateTime.now());
        sampleReservation.setSlot(slot);
    }

    @Test
    @DisplayName("Should return 0 penalty fee when vehicle checks out on or before booked end time")
    void testCalculatePenalty_OnTime_ShouldHaveZeroPenalty() {
        LocalDateTime checkOutTime = sampleReservation.getEndTime().minusMinutes(5);

        OverstayPenaltyService.PenaltyResult result = penaltyService.calculatePenalty(sampleReservation, checkOutTime);

        assertFalse(result.isOverstayed());
        assertEquals(0L, result.getOverstayMinutes());
        assertEquals(new BigDecimal("0.00"), result.getPenaltyFee());
        assertEquals(new BigDecimal("20.00"), result.getTotalAmount());
    }

    @Test
    @DisplayName("Should waive penalty fee when vehicle checks out within 15-minute grace period")
    void testCalculatePenalty_WithinGracePeriod_ShouldHaveZeroPenalty() {
        LocalDateTime checkOutTime = sampleReservation.getEndTime().plusMinutes(10);

        OverstayPenaltyService.PenaltyResult result = penaltyService.calculatePenalty(sampleReservation, checkOutTime);

        assertFalse(result.isOverstayed());
        assertEquals(10L, result.getOverstayMinutes());
        assertEquals(new BigDecimal("0.00"), result.getPenaltyFee());
        assertEquals(new BigDecimal("20.00"), result.getTotalAmount());
    }

    @Test
    @DisplayName("Should charge 1.5x hourly penalty when vehicle exceeds grace period")
    void testCalculatePenalty_AfterGracePeriod_ShouldApplyMultiplierPenalty() {
        // Overstay by 45 minutes (> 15 min grace period) -> rounds up to 1 billable hour
        // Hourly rate: $10.00 * 1.5 = $15.00
        // Penalty: $15.00, Total: $20.00 + $15.00 = $35.00
        LocalDateTime checkOutTime = sampleReservation.getEndTime().plusMinutes(45);

        OverstayPenaltyService.PenaltyResult result = penaltyService.calculatePenalty(sampleReservation, checkOutTime);

        assertTrue(result.isOverstayed());
        assertEquals(45L, result.getOverstayMinutes());
        assertEquals(1L, result.getBillableHours());
        assertEquals(new BigDecimal("15.00"), result.getPenaltyFee());
        assertEquals(new BigDecimal("35.00"), result.getTotalAmount());
    }
}
