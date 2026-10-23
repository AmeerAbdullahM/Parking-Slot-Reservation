package com.parkpulse.parkingreservation.service;

import com.parkpulse.parkingreservation.dto.ParkingSlotRequest;
import com.parkpulse.parkingreservation.dto.ParkingSlotResponse;
import com.parkpulse.parkingreservation.dto.SlotMapResponse;
import com.parkpulse.parkingreservation.exception.BadRequestException;
import com.parkpulse.parkingreservation.model.LocationType;
import com.parkpulse.parkingreservation.model.ParkingLot;
import com.parkpulse.parkingreservation.model.ParkingSlot;
import com.parkpulse.parkingreservation.model.SlotType;
import com.parkpulse.parkingreservation.repository.ParkingLotRepository;
import com.parkpulse.parkingreservation.repository.ParkingSlotRepository;
import com.parkpulse.parkingreservation.service.impl.ParkingSlotServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ParkingSlotServiceTest {

    @Mock
    private ParkingSlotRepository parkingSlotRepository;

    @Mock
    private ParkingLotRepository parkingLotRepository;

    @InjectMocks
    private ParkingSlotServiceImpl slotService;

    private ParkingLot sampleLot;
    private ParkingSlot sampleSlot;

    @BeforeEach
    void setUp() {
        sampleLot = new ParkingLot("Tech Mall", LocationType.MALL, "77 Market St", 2, 10, null);
        sampleLot.setId(1L);

        sampleSlot = new ParkingSlot("A-01", 1, 1, 1, SlotType.REGULAR, BigDecimal.valueOf(5.00), sampleLot);
        sampleSlot.setId(10L);
    }

    @Test
    @DisplayName("Should successfully create a slot in the 2D floor grid")
    void testCreateSlot_Success() {
        ParkingSlotRequest request = new ParkingSlotRequest();
        request.setSlotNumber("A-02");
        request.setFloorNumber(1);
        request.setRowNumber(1);
        request.setColNumber(2);
        request.setSlotType(SlotType.EV_CHARGING);
        request.setBaseHourlyRate(BigDecimal.valueOf(6.50));
        request.setParkingLotId(1L);

        when(parkingLotRepository.findById(1L)).thenReturn(Optional.of(sampleLot));
        when(parkingSlotRepository.findByParkingLotIdAndSlotNumber(1L, "A-02")).thenReturn(Optional.empty());
        when(parkingSlotRepository.save(any(ParkingSlot.class))).thenAnswer(inv -> {
            ParkingSlot s = inv.getArgument(0);
            s.setId(20L);
            return s;
        });

        ParkingSlotResponse response = slotService.createSlot(request);

        assertNotNull(response);
        assertEquals("A-02", response.getSlotNumber());
        assertEquals(SlotType.EV_CHARGING, response.getSlotType());
        verify(parkingSlotRepository, times(1)).save(any(ParkingSlot.class));
    }

    @Test
    @DisplayName("Should reject slot creation when duplicate slot number exists in the same lot")
    void testCreateSlot_DuplicateNumber_ThrowsBadRequestException() {
        ParkingSlotRequest request = new ParkingSlotRequest();
        request.setSlotNumber("A-01");
        request.setFloorNumber(1);
        request.setParkingLotId(1L);
        request.setBaseHourlyRate(BigDecimal.valueOf(5.00));

        when(parkingLotRepository.findById(1L)).thenReturn(Optional.of(sampleLot));
        when(parkingSlotRepository.findByParkingLotIdAndSlotNumber(1L, "A-01")).thenReturn(Optional.of(sampleSlot));

        assertThrows(BadRequestException.class, () -> slotService.createSlot(request));
        verify(parkingSlotRepository, never()).save(any(ParkingSlot.class));
    }

    @Test
    @DisplayName("Should return 2D floor slot map for visual bus-seat booking view")
    void testGetSlotMap_ReturnsGroupedFloors() {
        when(parkingLotRepository.findById(1L)).thenReturn(Optional.of(sampleLot));
        when(parkingSlotRepository.findByParkingLotIdAndFloorNumberOrderByRowNumberAscColNumberAsc(1L, 1))
                .thenReturn(List.of(sampleSlot));

        SlotMapResponse mapResponse = slotService.getSlotMap(1L, 1);

        assertNotNull(mapResponse);
        assertEquals(1, mapResponse.getSelectedFloor());
        assertEquals(2, mapResponse.getAvailableFloors().size());
        assertEquals(1, mapResponse.getSlots().size());
        assertEquals("A-01", mapResponse.getSlots().get(0).getSlotNumber());
    }
}
