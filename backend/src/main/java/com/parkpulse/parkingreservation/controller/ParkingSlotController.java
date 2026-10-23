package com.parkpulse.parkingreservation.controller;

import com.parkpulse.parkingreservation.dto.ParkingSlotRequest;
import com.parkpulse.parkingreservation.dto.ParkingSlotResponse;
import com.parkpulse.parkingreservation.model.SlotStatus;
import com.parkpulse.parkingreservation.model.SlotType;
import com.parkpulse.parkingreservation.service.ParkingSlotService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/slots")
public class ParkingSlotController {

    private final ParkingSlotService parkingSlotService;

    public ParkingSlotController(ParkingSlotService parkingSlotService) {
        this.parkingSlotService = parkingSlotService;
    }

    @GetMapping
    public ResponseEntity<Page<ParkingSlotResponse>> searchSlots(
            @RequestParam(required = false) Long lotId,
            @RequestParam(required = false) Integer floor,
            @RequestParam(required = false) SlotType type,
            @RequestParam(required = false) SlotStatus status,
            @PageableDefault(size = 12, sort = "slotNumber") Pageable pageable) {
        Page<ParkingSlotResponse> page = parkingSlotService.searchSlots(lotId, floor, type, status, pageable);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ParkingSlotResponse> getSlotById(@PathVariable Long id) {
        return ResponseEntity.ok(parkingSlotService.getSlotById(id));
    }

    @GetMapping("/lot/{lotId}")
    public ResponseEntity<List<ParkingSlotResponse>> getSlotsByLot(@PathVariable Long lotId) {
        return ResponseEntity.ok(parkingSlotService.getSlotsByLot(lotId));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ParkingSlotResponse> createSlot(@Valid @RequestBody ParkingSlotRequest request) {
        ParkingSlotResponse response = parkingSlotService.createSlot(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ParkingSlotResponse> updateSlot(
            @PathVariable Long id,
            @Valid @RequestBody ParkingSlotRequest request) {
        return ResponseEntity.ok(parkingSlotService.updateSlot(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Void> deleteSlot(@PathVariable Long id) {
        parkingSlotService.deleteSlot(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ParkingSlotResponse> updateStatus(
            @PathVariable Long id,
            @RequestParam SlotStatus status) {
        return ResponseEntity.ok(parkingSlotService.updateSlotStatus(id, status));
    }
}
