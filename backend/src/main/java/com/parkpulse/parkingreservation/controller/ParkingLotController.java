package com.parkpulse.parkingreservation.controller;

import com.parkpulse.parkingreservation.dto.ParkingLotRequest;
import com.parkpulse.parkingreservation.dto.ParkingLotResponse;
import com.parkpulse.parkingreservation.dto.SlotMapResponse;
import com.parkpulse.parkingreservation.service.ParkingLotService;
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
@RequestMapping("/api/lots")
public class ParkingLotController {

    private final ParkingLotService parkingLotService;
    private final ParkingSlotService parkingSlotService;

    public ParkingLotController(ParkingLotService parkingLotService,
                                ParkingSlotService parkingSlotService) {
        this.parkingLotService = parkingLotService;
        this.parkingSlotService = parkingSlotService;
    }

    @GetMapping
    public ResponseEntity<List<ParkingLotResponse>> getAllLots() {
        return ResponseEntity.ok(parkingLotService.getAllActiveLots());
    }

    @GetMapping("/search")
    public ResponseEntity<Page<ParkingLotResponse>> searchLots(
            @RequestParam(required = false) String name,
            @PageableDefault(size = 10, sort = "name") Pageable pageable) {
        return ResponseEntity.ok(parkingLotService.searchLots(name, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ParkingLotResponse> getLotById(@PathVariable Long id) {
        return ResponseEntity.ok(parkingLotService.getParkingLotById(id));
    }

    @GetMapping("/{id}/slots/map")
    public ResponseEntity<SlotMapResponse> getSlotMap(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "1") Integer floor) {
        return ResponseEntity.ok(parkingSlotService.getSlotMap(id, floor));
    }

    @GetMapping("/manager/{managerId}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<List<ParkingLotResponse>> getLotsByManager(@PathVariable Long managerId) {
        return ResponseEntity.ok(parkingLotService.getLotsByManager(managerId));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ParkingLotResponse> createLot(@Valid @RequestBody ParkingLotRequest request) {
        ParkingLotResponse response = parkingLotService.createParkingLot(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ParkingLotResponse> updateLot(
            @PathVariable Long id,
            @Valid @RequestBody ParkingLotRequest request) {
        return ResponseEntity.ok(parkingLotService.updateParkingLot(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteLot(@PathVariable Long id) {
        parkingLotService.deleteParkingLot(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/floors")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ParkingLotResponse> addFloor(@PathVariable Long id) {
        return ResponseEntity.ok(parkingLotService.addFloor(id));
    }
}
