package com.parkpulse.parkingreservation.dto;

import com.parkpulse.parkingreservation.model.LocationType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Set;

public class ParkingLotRequest {

    @NotBlank(message = "Facility name is required")
    private String name;

    @NotNull(message = "Location type is required")
    private LocationType locationType;

    @NotBlank(message = "Address is required")
    private String address;

    @NotNull(message = "Total floors count is required")
    @Min(value = 1, message = "Total floors must be at least 1")
    private Integer totalFloors;

    @Min(value = 0, message = "Total capacity cannot be negative")
    private Integer totalCapacity;

    private String imageUrl;

    private Long managerId;

    private Set<Long> amenityIds;

    public ParkingLotRequest() {
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

    public Long getManagerId() {
        return managerId;
    }

    public void setManagerId(Long managerId) {
        this.managerId = managerId;
    }

    public Set<Long> getAmenityIds() {
        return amenityIds;
    }

    public void setAmenityIds(Set<Long> amenityIds) {
        this.amenityIds = amenityIds;
    }
}
