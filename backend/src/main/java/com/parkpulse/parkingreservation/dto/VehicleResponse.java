package com.parkpulse.parkingreservation.dto;

import com.parkpulse.parkingreservation.model.VehicleType;

public class VehicleResponse {

    private Long id;
    private String licensePlate;
    private VehicleType vehicleType;
    private String model;
    private Long userId;

    public VehicleResponse() {
    }

    public VehicleResponse(Long id, String licensePlate, VehicleType vehicleType, String model, Long userId) {
        this.id = id;
        this.licensePlate = licensePlate;
        this.vehicleType = vehicleType;
        this.model = model;
        this.userId = userId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    public VehicleType getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(VehicleType vehicleType) {
        this.vehicleType = vehicleType;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
