package com.parkpulse.parkingreservation.service;

import com.parkpulse.parkingreservation.dto.AuthRequest;
import com.parkpulse.parkingreservation.dto.AuthResponse;
import com.parkpulse.parkingreservation.dto.CreateManagerRequest;
import com.parkpulse.parkingreservation.dto.RegisterRequest;
import com.parkpulse.parkingreservation.model.User;

public interface AuthService {
    AuthResponse registerCustomer(RegisterRequest request);
    AuthResponse createManager(CreateManagerRequest request);
    AuthResponse login(AuthRequest request);
    User getCurrentAuthenticatedUser();
}
