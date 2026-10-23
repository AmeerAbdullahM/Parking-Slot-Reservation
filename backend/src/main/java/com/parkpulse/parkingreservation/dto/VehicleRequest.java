package com.parkpulse.parkingreservation.dto;

import com.parkpulse.parkingreservation.model.VehicleType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class VehicleRequest {

    @NotBlank(message = "License plate number is required")
    @Pattern(regexp = "^[A-Z0-9- ]{3,15}$", message = "License plate must be uppercase alphanumeric (e.g. ABC-1234)")
    private String licensePlate;

    @NotNull(message = "Vehicle type is required")
    private VehicleType vehicleType = VehicleType.SEDAN;

    private String model;

    public VehicleRequest() {
    }

    public VehicleRequest(String licensePlate, VehicleType vehicleType, String model) {
        this.licensePlate = licensePlate;
        this.vehicleType = vehicleType;
        this.model = model;
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
}
