package com.parkpulse.parkingreservation.service;

import com.parkpulse.parkingreservation.dto.ParkingSlotRequest;
import com.parkpulse.parkingreservation.dto.ParkingSlotResponse;
import com.parkpulse.parkingreservation.dto.SlotMapResponse;
import com.parkpulse.parkingreservation.model.SlotStatus;
import com.parkpulse.parkingreservation.model.SlotType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ParkingSlotService {
    ParkingSlotResponse createSlot(ParkingSlotRequest request);
    ParkingSlotResponse updateSlot(Long id, ParkingSlotRequest request);
    void deleteSlot(Long id);
    ParkingSlotResponse getSlotById(Long id);
    SlotMapResponse getSlotMap(Long lotId, Integer floorNumber);
    Page<ParkingSlotResponse> searchSlots(Long lotId, Integer floor, SlotType type, SlotStatus status, Pageable pageable);
    ParkingSlotResponse updateSlotStatus(Long id, SlotStatus status);
    List<ParkingSlotResponse> getSlotsByLot(Long lotId);
}
