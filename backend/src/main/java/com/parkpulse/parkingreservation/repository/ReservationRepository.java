package com.parkpulse.parkingreservation.repository;

import com.parkpulse.parkingreservation.model.Reservation;
import com.parkpulse.parkingreservation.model.ReservationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    Optional<Reservation> findByReservationCode(String reservationCode);

    Page<Reservation> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Page<Reservation> findBySlotParkingLotIdOrderByCreatedAtDesc(Long lotId, Pageable pageable);

    @Query("SELECT r FROM Reservation r WHERE r.slot.id = :slotId " +
           "AND r.status IN (com.parkpulse.parkingreservation.model.ReservationStatus.CONFIRMED, com.parkpulse.parkingreservation.model.ReservationStatus.CHECKED_IN) " +
           "AND (:startTime < r.endTime AND :endTime > r.startTime)")
    List<Reservation> findOverlappingReservations(
            @Param("slotId") Long slotId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    @Query("SELECT r FROM Reservation r WHERE r.slot.parkingLot.id = :lotId " +
           "AND r.status IN (com.parkpulse.parkingreservation.model.ReservationStatus.CONFIRMED, com.parkpulse.parkingreservation.model.ReservationStatus.CHECKED_IN) " +
           "AND (:startTime < r.endTime AND :endTime > r.startTime)")
    List<Reservation> findLotOverlappingReservations(
            @Param("lotId") Long lotId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    @Query("SELECT r FROM Reservation r WHERE " +
           "(:lotId IS NULL OR r.slot.parkingLot.id = :lotId) AND " +
           "(:status IS NULL OR r.status = :status) AND " +
           "(:search IS NULL OR LOWER(r.reservationCode) LIKE LOWER(CONCAT('%', :search, '%')) " +
           " OR LOWER(r.vehicle.licensePlate) LIKE LOWER(CONCAT('%', :search, '%')) " +
           " OR LOWER(r.user.fullName) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Reservation> searchReservations(
            @Param("lotId") Long lotId,
            @Param("status") ReservationStatus status,
            @Param("search") String search,
            Pageable pageable
    );
}
