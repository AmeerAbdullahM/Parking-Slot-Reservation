package com.parkpulse.parkingreservation.dto;

import com.parkpulse.parkingreservation.model.LocationType;
import java.util.List;

public class ParkingLotResponse {

    private Long id;
    private String name;
    private LocationType locationType;
    private String address;
    private Integer totalFloors;
    private Integer totalCapacity;
    private String imageUrl;
    private Boolean isActive;
    private Long managerId;
    private String managerName;
    private String managerEmail;
    private List<AmenityResponse> amenities;
    private Long totalSlots;
    private Long availableSlots;

    public ParkingLotResponse() {
    }

    public static class AmenityResponse {
        private Long id;
        private String name;
        private String iconCode;
        private String description;

        public AmenityResponse() {
        }

        public AmenityResponse(Long id, String name, String iconCode, String description) {
            this.id = id;
            this.name = name;
            this.iconCode = iconCode;
            this.description = description;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getIconCode() {
            return iconCode;
        }

        public void setIconCode(String iconCode) {
            this.iconCode = iconCode;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocationType getLocationType() {
        return locationType;
    }

    public void setLocationType(LocationType locationType) {
        this.locationType = locationType;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Integer getTotalFloors() {
        return totalFloors;
    }

    public void setTotalFloors(Integer totalFloors) {
        this.totalFloors = totalFloors;
    }

    public Integer getTotalCapacity() {
        return totalCapacity;
    }

    public void setTotalCapacity(Integer totalCapacity) {
        this.totalCapacity = totalCapacity;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Long getManagerId() {
        return managerId;
    }

    public void setManagerId(Long managerId) {
        this.managerId = managerId;
    }

    public String getManagerName() {
        return managerName;
    }

    public void setManagerName(String managerName) {
        this.managerName = managerName;
    }

    public String getManagerEmail() {
        return managerEmail;
    }

    public void setManagerEmail(String managerEmail) {
        this.managerEmail = managerEmail;
    }

    public List<AmenityResponse> getAmenities() {
        return amenities;
    }

    public void setAmenities(List<AmenityResponse> amenities) {
        this.amenities = amenities;
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
}
