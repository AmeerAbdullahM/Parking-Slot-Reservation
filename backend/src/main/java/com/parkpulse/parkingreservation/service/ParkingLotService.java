package com.parkpulse.parkingreservation.service;

import com.parkpulse.parkingreservation.dto.ParkingLotRequest;
import com.parkpulse.parkingreservation.dto.ParkingLotResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ParkingLotService {
    ParkingLotResponse createParkingLot(ParkingLotRequest request);
    ParkingLotResponse updateParkingLot(Long id, ParkingLotRequest request);
    void deleteParkingLot(Long id);
    ParkingLotResponse getParkingLotById(Long id);
    List<ParkingLotResponse> getAllActiveLots();
    List<ParkingLotResponse> getLotsByManager(Long managerId);
    Page<ParkingLotResponse> searchLots(String name, Pageable pageable);
    ParkingLotResponse addFloor(Long lotId);
}
