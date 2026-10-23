package com.parkpulse.parkingreservation.dto;

import java.util.List;

public class SlotMapResponse {

    private Long lotId;
    private String lotName;
    private Integer selectedFloor;
    private List<Integer> availableFloors;
    private List<ParkingSlotResponse> slots;
    private Long totalSlotsOnFloor;
    private Long availableSlotsOnFloor;

    public SlotMapResponse() {
    }

    public SlotMapResponse(Long lotId, String lotName, Integer selectedFloor, List<Integer> availableFloors,
                           List<ParkingSlotResponse> slots, Long totalSlotsOnFloor, Long availableSlotsOnFloor) {
        this.lotId = lotId;
        this.lotName = lotName;
        this.selectedFloor = selectedFloor;
        this.availableFloors = availableFloors;
        this.slots = slots;
        this.totalSlotsOnFloor = totalSlotsOnFloor;
        this.availableSlotsOnFloor = availableSlotsOnFloor;
    }

    public Long getLotId() {
        return lotId;
    }

    public void setLotId(Long lotId) {
        this.lotId = lotId;
    }

    public String getLotName() {
        return lotName;
    }

    public void setLotName(String lotName) {
        this.lotName = lotName;
    }

    public Integer getSelectedFloor() {
        return selectedFloor;
    }

    public void setSelectedFloor(Integer selectedFloor) {
        this.selectedFloor = selectedFloor;
    }

    public List<Integer> getAvailableFloors() {
        return availableFloors;
    }

    public void setAvailableFloors(List<Integer> availableFloors) {
        this.availableFloors = availableFloors;
    }

    public List<ParkingSlotResponse> getSlots() {
        return slots;
    }

    public void setSlots(List<ParkingSlotResponse> slots) {
        this.slots = slots;
    }

    public Long getTotalSlotsOnFloor() {
        return totalSlotsOnFloor;
    }

    public void setTotalSlotsOnFloor(Long totalSlotsOnFloor) {
        this.totalSlotsOnFloor = totalSlotsOnFloor;
    }

    public Long getAvailableSlotsOnFloor() {
        return availableSlotsOnFloor;
    }

    public void setAvailableSlotsOnFloor(Long availableSlotsOnFloor) {
        this.availableSlotsOnFloor = availableSlotsOnFloor;
    }
}
