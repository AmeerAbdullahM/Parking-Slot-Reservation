package com.parkpulse.parkingreservation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.parkpulse.parkingreservation.model.VehicleType;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class ReservationRequest {

    @NotNull(message = "Slot ID is required")
    private Long slotId;

    // Optional if licensePlate is provided directly
    private Long vehicleId;

    // Direct vehicle input fields
    private String licensePlate;

    private String vehicleModel;

    private VehicleType vehicleType = VehicleType.SEDAN;

    @NotNull(message = "Start time is required")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startTime;

    @NotNull(message = "End time is required")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime endTime;

    public ReservationRequest() {
    }

    public ReservationRequest(Long slotId, Long vehicleId, LocalDateTime startTime, LocalDateTime endTime) {
        this.slotId = slotId;
        this.vehicleId = vehicleId;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public ReservationRequest(Long slotId, String licensePlate, String vehicleModel, VehicleType vehicleType, LocalDateTime startTime, LocalDateTime endTime) {
        this.slotId = slotId;
        this.licensePlate = licensePlate;
        this.vehicleModel = vehicleModel;
        this.vehicleType = vehicleType;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public Long getSlotId() {
        return slotId;
    }

    public void setSlotId(Long slotId) {
        this.slotId = slotId;
    }

    public Long getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(Long vehicleId) {
        this.vehicleId = vehicleId;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    public String getVehicleModel() {
        return vehicleModel;
    }

    public void setVehicleModel(String vehicleModel) {
        this.vehicleModel = vehicleModel;
    }

    public VehicleType getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(VehicleType vehicleType) {
        this.vehicleType = vehicleType;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
}

