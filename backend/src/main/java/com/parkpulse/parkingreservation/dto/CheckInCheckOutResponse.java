package com.parkpulse.parkingreservation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.parkpulse.parkingreservation.model.ReservationStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CheckInCheckOutResponse {

    private String reservationCode;
    private ReservationStatus status;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime actionTime;

    private Long overstayMinutes;
    private BigDecimal penaltyFee;
    private BigDecimal totalAmount;
    private String message;

    public CheckInCheckOutResponse() {
    }

    public CheckInCheckOutResponse(String reservationCode, ReservationStatus status, LocalDateTime actionTime,
                                  Long overstayMinutes, BigDecimal penaltyFee, BigDecimal totalAmount, String message) {
        this.reservationCode = reservationCode;
        this.status = status;
        this.actionTime = actionTime;
        this.overstayMinutes = overstayMinutes;
        this.penaltyFee = penaltyFee;
        this.totalAmount = totalAmount;
        this.message = message;
    }

    public String getReservationCode() {
        return reservationCode;
    }

    public void setReservationCode(String reservationCode) {
        this.reservationCode = reservationCode;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public void setStatus(ReservationStatus status) {
        this.status = status;
    }

    public LocalDateTime getActionTime() {
        return actionTime;
    }

    public void setActionTime(LocalDateTime actionTime) {
        this.actionTime = actionTime;
    }

    public Long getOverstayMinutes() {
        return overstayMinutes;
    }

    public void setOverstayMinutes(Long overstayMinutes) {
        this.overstayMinutes = overstayMinutes;
    }

    public BigDecimal getPenaltyFee() {
        return penaltyFee;
    }

    public void setPenaltyFee(BigDecimal penaltyFee) {
        this.penaltyFee = penaltyFee;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
