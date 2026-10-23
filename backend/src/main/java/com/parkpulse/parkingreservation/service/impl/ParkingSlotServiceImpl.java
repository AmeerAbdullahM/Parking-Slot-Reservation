package com.parkpulse.parkingreservation.service.impl;

import com.parkpulse.parkingreservation.dto.ParkingSlotRequest;
import com.parkpulse.parkingreservation.dto.ParkingSlotResponse;
import com.parkpulse.parkingreservation.dto.SlotMapResponse;
import com.parkpulse.parkingreservation.exception.BadRequestException;
import com.parkpulse.parkingreservation.exception.ResourceNotFoundException;
import com.parkpulse.parkingreservation.model.ParkingLot;
import com.parkpulse.parkingreservation.model.ParkingSlot;
import com.parkpulse.parkingreservation.model.SlotStatus;
import com.parkpulse.parkingreservation.model.SlotType;
import com.parkpulse.parkingreservation.repository.ParkingLotRepository;
import com.parkpulse.parkingreservation.repository.ParkingSlotRepository;
import com.parkpulse.parkingreservation.service.ParkingSlotService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ParkingSlotServiceImpl implements ParkingSlotService {

    private final ParkingSlotRepository parkingSlotRepository;
    private final ParkingLotRepository parkingLotRepository;

    public ParkingSlotServiceImpl(ParkingSlotRepository parkingSlotRepository,
                                  ParkingLotRepository parkingLotRepository) {
        this.parkingSlotRepository = parkingSlotRepository;
        this.parkingLotRepository = parkingLotRepository;
    }

    @Override
    @Transactional
    public ParkingSlotResponse createSlot(ParkingSlotRequest request) {
        ParkingLot lot = parkingLotRepository.findById(request.getParkingLotId())
                .orElseThrow(() -> new ResourceNotFoundException("Parking Lot not found with ID: " + request.getParkingLotId()));

        if (parkingSlotRepository.findByParkingLotIdAndSlotNumber(lot.getId(), request.getSlotNumber().trim()).isPresent()) {
            throw new BadRequestException("Slot with number '" + request.getSlotNumber() + "' already exists in this lot");
        }

        ParkingSlot slot = new ParkingSlot();
        slot.setSlotNumber(request.getSlotNumber().trim().toUpperCase());
        slot.setFloorNumber(request.getFloorNumber());
        slot.setRowNumber(request.getRowNumber() != null ? request.getRowNumber() : 1);
        slot.setColNumber(request.getColNumber() != null ? request.getColNumber() : 1);
        slot.setSlotType(request.getSlotType() != null ? request.getSlotType() : SlotType.REGULAR);
        slot.setBaseHourlyRate(request.getBaseHourlyRate());
        slot.setStatus(SlotStatus.AVAILABLE);
        slot.setParkingLot(lot);

        ParkingSlot saved = parkingSlotRepository.save(slot);

        // Update lot capacity
        lot.setTotalCapacity((int) parkingSlotRepository.countByParkingLotId(lot.getId()));
        parkingLotRepository.save(lot);

        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public ParkingSlotResponse updateSlot(Long id, ParkingSlotRequest request) {
        ParkingSlot slot = parkingSlotRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Parking Slot not found with ID: " + id));

        slot.setSlotNumber(request.getSlotNumber().trim().toUpperCase());
        slot.setFloorNumber(request.getFloorNumber());
        if (request.getRowNumber() != null) {
            slot.setRowNumber(request.getRowNumber());
        }
        if (request.getColNumber() != null) {
            slot.setColNumber(request.getColNumber());
        }
        if (request.getSlotType() != null) {
            slot.setSlotType(request.getSlotType());
        }
        if (request.getBaseHourlyRate() != null) {
            slot.setBaseHourlyRate(request.getBaseHourlyRate());
        }

        ParkingSlot saved = parkingSlotRepository.save(slot);
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public void deleteSlot(Long id) {
        ParkingSlot slot = parkingSlotRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Parking Slot not found with ID: " + id));
        ParkingLot lot = slot.getParkingLot();
        parkingSlotRepository.delete(slot);

        lot.setTotalCapacity((int) parkingSlotRepository.countByParkingLotId(lot.getId()));
        parkingLotRepository.save(lot);
    }

    @Override
    @Transactional(readOnly = true)
    public ParkingSlotResponse getSlotById(Long id) {
        ParkingSlot slot = parkingSlotRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Parking Slot not found with ID: " + id));
        return mapToResponse(slot);
    }

    @Override
    @Transactional(readOnly = true)
    public SlotMapResponse getSlotMap(Long lotId, Integer floorNumber) {
        ParkingLot lot = parkingLotRepository.findById(lotId)
                .orElseThrow(() -> new ResourceNotFoundException("Parking Lot not found with ID: " + lotId));

        int targetFloor = (floorNumber != null) ? floorNumber : 1;

        List<ParkingSlot> slotsOnFloor = parkingSlotRepository
                .findByParkingLotIdAndFloorNumberOrderByRowNumberAscColNumberAsc(lotId, targetFloor);

        List<ParkingSlotResponse> slotResponses = slotsOnFloor.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        long totalOnFloor = slotsOnFloor.size();
        long availableOnFloor = slotsOnFloor.stream()
                .filter(s -> s.getStatus() == SlotStatus.AVAILABLE)
                .count();

        List<Integer> floors = new ArrayList<>();
        for (int i = 1; i <= lot.getTotalFloors(); i++) {
            floors.add(i);
        }

        return new SlotMapResponse(
                lot.getId(),
                lot.getName(),
                targetFloor,
                floors,
                slotResponses,
                totalOnFloor,
                availableOnFloor
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ParkingSlotResponse> searchSlots(Long lotId, Integer floor, SlotType type, SlotStatus status, Pageable pageable) {
        return parkingSlotRepository.searchSlots(lotId, floor, type, status, pageable)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional
    public ParkingSlotResponse updateSlotStatus(Long id, SlotStatus status) {
        ParkingSlot slot = parkingSlotRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Parking Slot not found with ID: " + id));
        slot.setStatus(status);
        ParkingSlot saved = parkingSlotRepository.save(slot);
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParkingSlotResponse> getSlotsByLot(Long lotId) {
        return parkingSlotRepository.findByParkingLotIdOrderByFloorNumberAscRowNumberAscColNumberAsc(lotId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private ParkingSlotResponse mapToResponse(ParkingSlot slot) {
        ParkingSlotResponse response = new ParkingSlotResponse();
        response.setId(slot.getId());
        response.setSlotNumber(slot.getSlotNumber());
        response.setFloorNumber(slot.getFloorNumber());
        response.setRowNumber(slot.getRowNumber());
        response.setColNumber(slot.getColNumber());
        response.setSlotType(slot.getSlotType());
        response.setBaseHourlyRate(slot.getBaseHourlyRate());
        response.setStatus(slot.getStatus());
        if (slot.getParkingLot() != null) {
            response.setParkingLotId(slot.getParkingLot().getId());
            response.setParkingLotName(slot.getParkingLot().getName());
        }
        return response;
    }
}
