package com.parkpulse.parkingreservation.dto;

import java.math.BigDecimal;

public class DashboardStatsResponse {

    private Long totalLots;
    private Long totalSlots;
    private Long availableSlots;
    private Long occupiedSlots;
    private Long totalReservations;
    private Long activeReservations;
    private BigDecimal totalRevenue = BigDecimal.ZERO;
    private BigDecimal totalPenalties = BigDecimal.ZERO;
    private Long totalManagers;
    private Long totalCustomers;

    public DashboardStatsResponse() {
    }

    public Long getTotalLots() {
        return totalLots;
    }

    public void setTotalLots(Long totalLots) {
        this.totalLots = totalLots;
    }

    public Long getTotalSlots() {
        return totalSlots;
    }

    public void setTotalSlots(Long totalSlots) {
        this.totalSlots = totalSlots;
    }

    public Long getAvailableSlots() {
        return availableSlots;
    }

    public void setAvailableSlots(Long availableSlots) {
        this.availableSlots = availableSlots;
    }

    public Long getOccupiedSlots() {
        return occupiedSlots;
    }

    public void setOccupiedSlots(Long occupiedSlots) {
        this.occupiedSlots = occupiedSlots;
    }

    public Long getTotalReservations() {
        return totalReservations;
    }

    public void setTotalReservations(Long totalReservations) {
        this.totalReservations = totalReservations;
    }

    public Long getActiveReservations() {
        return activeReservations;
    }

    public void setActiveReservations(Long activeReservations) {
        this.activeReservations = activeReservations;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public BigDecimal getTotalPenalties() {
        return totalPenalties;
    }

    public void setTotalPenalties(BigDecimal totalPenalties) {
        this.totalPenalties = totalPenalties;
    }

    public Long getTotalManagers() {
        return totalManagers;
    }

    public void setTotalManagers(Long totalManagers) {
        this.totalManagers = totalManagers;
    }

    public Long getTotalCustomers() {
        return totalCustomers;
    }

    public void setTotalCustomers(Long totalCustomers) {
        this.totalCustomers = totalCustomers;
    }
}
