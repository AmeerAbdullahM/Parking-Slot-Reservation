package com.parkpulse.parkingreservation.dto;

import com.parkpulse.parkingreservation.model.Role;

public class AuthResponse {

    private String token;
    private String tokenType = "Bearer";
    private Long userId;
    private String fullName;
    private String email;
    private Role role;
    private Long assignedLotId;

    public AuthResponse() {
    }

    public AuthResponse(String token, Long userId, String fullName, String email, Role role, Long assignedLotId) {
        this.token = token;
        this.tokenType = "Bearer";
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.assignedLotId = assignedLotId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
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

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Long getAssignedLotId() {
        return assignedLotId;
    }

    public void setAssignedLotId(Long assignedLotId) {
        this.assignedLotId = assignedLotId;
    }
}
