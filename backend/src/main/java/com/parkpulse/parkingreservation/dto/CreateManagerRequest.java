package com.parkpulse.parkingreservation.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateManagerRequest {

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    private String phone;

    private Long assignedLotId;

    public CreateManagerRequest() {
    }

    public CreateManagerRequest(String fullName, String email, String password, String phone, Long assignedLotId) {
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.assignedLotId = assignedLotId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Long getAssignedLotId() {
        return assignedLotId;
    }

    public void setAssignedLotId(Long assignedLotId) {
        this.assignedLotId = assignedLotId;
    }
}
