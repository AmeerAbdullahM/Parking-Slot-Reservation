package com.parkpulse.parkingreservation.dto;

import com.parkpulse.parkingreservation.model.SlotType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class ParkingSlotRequest {

    @NotBlank(message = "Slot number is required")
    private String slotNumber;

    @NotNull(message = "Floor number is required")
    @Min(value = 1, message = "Floor number must be at least 1")
    private Integer floorNumber;

    @Min(value = 1, message = "Row number must be at least 1")
    private Integer rowNumber = 1;

    @Min(value = 1, message = "Column number must be at least 1")
    private Integer colNumber = 1;

    @NotNull(message = "Slot type is required")
    private SlotType slotType = SlotType.REGULAR;

    @NotNull(message = "Base hourly rate is required")
    @DecimalMin(value = "0.00", message = "Hourly rate cannot be negative")
    private BigDecimal baseHourlyRate;

    @NotNull(message = "Parking lot ID is required")
    private Long parkingLotId;

    public ParkingSlotRequest() {
    }

    public String getSlotNumber() {
        return slotNumber;
    }

    public void setSlotNumber(String slotNumber) {
        this.slotNumber = slotNumber;
    }

    public Integer getFloorNumber() {
        return floorNumber;
    }

    public void setFloorNumber(Integer floorNumber) {
        this.floorNumber = floorNumber;
    }

    public Integer getRowNumber() {
        return rowNumber;
    }

    public void setRowNumber(Integer rowNumber) {
        this.rowNumber = rowNumber;
    }

    public Integer getColNumber() {
        return colNumber;
    }

    public void setColNumber(Integer colNumber) {
        this.colNumber = colNumber;
    }

    public SlotType getSlotType() {
        return slotType;
    }

    public void setSlotType(SlotType slotType) {
        this.slotType = slotType;
    }

    public BigDecimal getBaseHourlyRate() {
        return baseHourlyRate;
    }

    public void setBaseHourlyRate(BigDecimal baseHourlyRate) {
        this.baseHourlyRate = baseHourlyRate;
    }

    public Long getParkingLotId() {
        return parkingLotId;
    }

    public void setParkingLotId(Long parkingLotId) {
        this.parkingLotId = parkingLotId;
    }
}
