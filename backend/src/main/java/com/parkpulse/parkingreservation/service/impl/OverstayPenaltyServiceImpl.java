package com.parkpulse.parkingreservation.service.impl;

import com.parkpulse.parkingreservation.model.Reservation;
import com.parkpulse.parkingreservation.service.OverstayPenaltyService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class OverstayPenaltyServiceImpl implements OverstayPenaltyService {

    @Value("${app.parking.grace-period-minutes:15}")
    private long gracePeriodMinutes;

    @Value("${app.parking.penalty-multiplier:1.5}")
    private double penaltyMultiplier;

    @Override
    public PenaltyResult calculatePenalty(Reservation reservation, LocalDateTime actualCheckOutTime) {
        BigDecimal baseFee = reservation.getBaseFee() != null ? reservation.getBaseFee() : BigDecimal.ZERO;
        BigDecimal slotRate = reservation.getSlot().getBaseHourlyRate();

        if (actualCheckOutTime.isBefore(reservation.getEndTime()) || actualCheckOutTime.isEqual(reservation.getEndTime())) {
            return new PenaltyResult(
                    BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP),
                    baseFee.setScale(2, RoundingMode.HALF_UP),
                    0L,
                    0L,
                    false,
                    "Checked out on time. No overstay penalty applied."
            );
        }

        long minutesOverstayed = Duration.between(reservation.getEndTime(), actualCheckOutTime).toMinutes();

        if (minutesOverstayed <= gracePeriodMinutes) {
            return new PenaltyResult(
                    BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP),
                    baseFee.setScale(2, RoundingMode.HALF_UP),
                    minutesOverstayed,
                    0L,
                    false,
                    String.format("Checked out within %d min grace period (%d min overtime). No penalty applied.",
                            gracePeriodMinutes, minutesOverstayed)
            );
        }

        // Billable overtime hours rounded up
        long billableHours = (long) Math.ceil((double) minutesOverstayed / 60.0);

        BigDecimal hourlyPenaltyRate = slotRate.multiply(BigDecimal.valueOf(penaltyMultiplier));
        BigDecimal penaltyFee = hourlyPenaltyRate.multiply(BigDecimal.valueOf(billableHours)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalAmount = baseFee.add(penaltyFee).setScale(2, RoundingMode.HALF_UP);

        String message = String.format("Overstay detected! Exceeded booked time by %d minutes (%d billable hour(s) at %.1fx rate of $%.2f/hr). Penalty fee: $%.2f. Total: $%.2f",
                minutesOverstayed, billableHours, penaltyMultiplier, hourlyPenaltyRate.doubleValue(), penaltyFee.doubleValue(), totalAmount.doubleValue());

        return new PenaltyResult(penaltyFee, totalAmount, minutesOverstayed, billableHours, true, message);
    }
}
