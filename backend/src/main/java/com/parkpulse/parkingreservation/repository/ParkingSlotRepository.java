package com.parkpulse.parkingreservation.repository;

import com.parkpulse.parkingreservation.model.ParkingSlot;
import com.parkpulse.parkingreservation.model.SlotStatus;
import com.parkpulse.parkingreservation.model.SlotType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParkingSlotRepository extends JpaRepository<ParkingSlot, Long> {

    List<ParkingSlot> findByParkingLotIdOrderByFloorNumberAscRowNumberAscColNumberAsc(Long lotId);

    List<ParkingSlot> findByParkingLotIdAndFloorNumberOrderByRowNumberAscColNumberAsc(Long lotId, Integer floorNumber);

    Optional<ParkingSlot> findByParkingLotIdAndSlotNumber(Long lotId, String slotNumber);

    long countByParkingLotIdAndStatus(Long lotId, SlotStatus status);

    long countByParkingLotId(Long lotId);

    @Query("SELECT s FROM ParkingSlot s WHERE " +
           "(:lotId IS NULL OR s.parkingLot.id = :lotId) AND " +
           "(:floor IS NULL OR s.floorNumber = :floor) AND " +
           "(:type IS NULL OR s.slotType = :type) AND " +
           "(:status IS NULL OR s.status = :status)")
    Page<ParkingSlot> searchSlots(
            @Param("lotId") Long lotId,
            @Param("floor") Integer floor,
            @Param("type") SlotType type,
            @Param("status") SlotStatus status,
            Pageable pageable
    );
}
