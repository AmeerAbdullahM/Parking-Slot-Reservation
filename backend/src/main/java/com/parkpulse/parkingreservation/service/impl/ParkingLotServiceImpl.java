package com.parkpulse.parkingreservation.service.impl;

import com.parkpulse.parkingreservation.dto.ParkingLotRequest;
import com.parkpulse.parkingreservation.dto.ParkingLotResponse;
import com.parkpulse.parkingreservation.exception.ResourceNotFoundException;
import com.parkpulse.parkingreservation.model.Amenity;
import com.parkpulse.parkingreservation.model.ParkingLot;
import com.parkpulse.parkingreservation.model.SlotStatus;
import com.parkpulse.parkingreservation.model.User;
import com.parkpulse.parkingreservation.repository.AmenityRepository;
import com.parkpulse.parkingreservation.repository.ParkingLotRepository;
import com.parkpulse.parkingreservation.repository.ParkingSlotRepository;
import com.parkpulse.parkingreservation.repository.UserRepository;
import com.parkpulse.parkingreservation.service.ParkingLotService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ParkingLotServiceImpl implements ParkingLotService {

    private final ParkingLotRepository parkingLotRepository;
    private final ParkingSlotRepository parkingSlotRepository;
    private final UserRepository userRepository;
    private final AmenityRepository amenityRepository;

    public ParkingLotServiceImpl(ParkingLotRepository parkingLotRepository,
                                 ParkingSlotRepository parkingSlotRepository,
                                 UserRepository userRepository,
                                 AmenityRepository amenityRepository) {
        this.parkingLotRepository = parkingLotRepository;
        this.parkingSlotRepository = parkingSlotRepository;
        this.userRepository = userRepository;
        this.amenityRepository = amenityRepository;
    }

    @Override
    @Transactional
    public ParkingLotResponse createParkingLot(ParkingLotRequest request) {
        ParkingLot lot = new ParkingLot();
        lot.setName(request.getName().trim());
        lot.setLocationType(request.getLocationType());
        lot.setAddress(request.getAddress().trim());
        lot.setTotalFloors(request.getTotalFloors() != null ? request.getTotalFloors() : 1);
        lot.setTotalCapacity(request.getTotalCapacity() != null ? request.getTotalCapacity() : 0);
        lot.setImageUrl(request.getImageUrl());
        lot.setIsActive(true);

        if (request.getManagerId() != null) {
            User manager = userRepository.findById(request.getManagerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Manager not found with ID: " + request.getManagerId()));
            lot.setManager(manager);
        }

        if (request.getAmenityIds() != null && !request.getAmenityIds().isEmpty()) {
            Set<Amenity> amenities = new HashSet<>(amenityRepository.findAllById(request.getAmenityIds()));
            lot.setAmenities(amenities);
        }

        ParkingLot savedLot = parkingLotRepository.save(lot);
        return mapToResponse(savedLot);
    }

    @Override
    @Transactional
    public ParkingLotResponse updateParkingLot(Long id, ParkingLotRequest request) {
        ParkingLot lot = parkingLotRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Parking Lot not found with ID: " + id));

        lot.setName(request.getName().trim());
        lot.setLocationType(request.getLocationType());
        lot.setAddress(request.getAddress().trim());
        if (request.getTotalFloors() != null) {
            lot.setTotalFloors(request.getTotalFloors());
        }
        if (request.getTotalCapacity() != null) {
            lot.setTotalCapacity(request.getTotalCapacity());
        }
        if (request.getImageUrl() != null) {
            lot.setImageUrl(request.getImageUrl());
        }

        if (request.getManagerId() != null) {
            User manager = userRepository.findById(request.getManagerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Manager not found with ID: " + request.getManagerId()));
            lot.setManager(manager);
        }

        if (request.getAmenityIds() != null) {
            Set<Amenity> amenities = new HashSet<>(amenityRepository.findAllById(request.getAmenityIds()));
            lot.setAmenities(amenities);
        }

        ParkingLot updatedLot = parkingLotRepository.save(lot);
        return mapToResponse(updatedLot);
    }

    @Override
    @Transactional
    public void deleteParkingLot(Long id) {
        ParkingLot lot = parkingLotRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Parking Lot not found with ID: " + id));
        lot.setIsActive(false);
        parkingLotRepository.save(lot);
    }

    @Override
    @Transactional(readOnly = true)
    public ParkingLotResponse getParkingLotById(Long id) {
        ParkingLot lot = parkingLotRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Parking Lot not found with ID: " + id));
        return mapToResponse(lot);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParkingLotResponse> getAllActiveLots() {
        return parkingLotRepository.findByIsActiveTrue().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParkingLotResponse> getLotsByManager(Long managerId) {
        return parkingLotRepository.findByManagerId(managerId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ParkingLotResponse> searchLots(String name, Pageable pageable) {
        Page<ParkingLot> page = (name != null && !name.trim().isEmpty())
                ? parkingLotRepository.findByNameContainingIgnoreCase(name.trim(), pageable)
                : parkingLotRepository.findAll(pageable);
        return page.map(this::mapToResponse);
    }

    @Override
    @Transactional
    public ParkingLotResponse addFloor(Long lotId) {
        ParkingLot lot = parkingLotRepository.findById(lotId)
                .orElseThrow(() -> new ResourceNotFoundException("Parking Lot not found with ID: " + lotId));
        lot.setTotalFloors(lot.getTotalFloors() + 1);
        ParkingLot saved = parkingLotRepository.save(lot);
        return mapToResponse(saved);
    }

    private ParkingLotResponse mapToResponse(ParkingLot lot) {
        ParkingLotResponse response = new ParkingLotResponse();
        response.setId(lot.getId());
        response.setName(lot.getName());
        response.setLocationType(lot.getLocationType());
        response.setAddress(lot.getAddress());
        response.setTotalFloors(lot.getTotalFloors());
        response.setTotalCapacity(lot.getTotalCapacity());
        response.setImageUrl(lot.getImageUrl());
        response.setIsActive(lot.getIsActive());

        if (lot.getManager() != null) {
            response.setManagerId(lot.getManager().getId());
            response.setManagerName(lot.getManager().getFullName());
            response.setManagerEmail(lot.getManager().getEmail());
        }

        if (lot.getAmenities() != null) {
            List<ParkingLotResponse.AmenityResponse> amenities = lot.getAmenities().stream()
                    .map(a -> new ParkingLotResponse.AmenityResponse(a.getId(), a.getName(), a.getIconCode(), a.getDescription()))
                    .collect(Collectors.toList());
            response.setAmenities(amenities);
        }

        long totalSlots = parkingSlotRepository.countByParkingLotId(lot.getId());
        long availableSlots = parkingSlotRepository.countByParkingLotIdAndStatus(lot.getId(), SlotStatus.AVAILABLE);

        response.setTotalSlots(totalSlots);
        response.setAvailableSlots(availableSlots);

        return response;
    }
}
