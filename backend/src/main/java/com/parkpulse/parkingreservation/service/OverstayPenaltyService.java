package com.parkpulse.parkingreservation.service;

import com.parkpulse.parkingreservation.model.Reservation;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface OverstayPenaltyService {

    class PenaltyResult {
        private final BigDecimal penaltyFee;
        private final BigDecimal totalAmount;
        private final long overstayMinutes;
        private final long billableHours;
        private final boolean overstayed;
        private final String message;

        public PenaltyResult(BigDecimal penaltyFee, BigDecimal totalAmount, long overstayMinutes,
                             long billableHours, boolean overstayed, String message) {
            this.penaltyFee = penaltyFee;
            this.totalAmount = totalAmount;
            this.overstayMinutes = overstayMinutes;
            this.billableHours = billableHours;
            this.overstayed = overstayed;
            this.message = message;
        }

        public BigDecimal getPenaltyFee() {
            return penaltyFee;
        }

        public BigDecimal getTotalAmount() {
            return totalAmount;
        }

        public long getOverstayMinutes() {
            return overstayMinutes;
        }

        public long getBillableHours() {
            return billableHours;
        }

        public boolean isOverstayed() {
            return overstayed;
        }

        public String getMessage() {
            return message;
        }
    }

    PenaltyResult calculatePenalty(Reservation reservation, LocalDateTime actualCheckOutTime);
}
