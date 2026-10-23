package com.parkpulse.parkingreservation.repository;

import com.parkpulse.parkingreservation.model.ParkingLot;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ParkingLotRepository extends JpaRepository<ParkingLot, Long> {
    List<ParkingLot> findByIsActiveTrue();
    List<ParkingLot> findByManagerId(Long managerId);
    Page<ParkingLot> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
