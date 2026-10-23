package com.parkpulse.parkingreservation.service;

import com.parkpulse.parkingreservation.dto.AuthResponse;
import com.parkpulse.parkingreservation.dto.RegisterRequest;
import com.parkpulse.parkingreservation.exception.BadRequestException;
import com.parkpulse.parkingreservation.model.Role;
import com.parkpulse.parkingreservation.model.User;
import com.parkpulse.parkingreservation.repository.ParkingLotRepository;
import com.parkpulse.parkingreservation.repository.UserRepository;
import com.parkpulse.parkingreservation.security.JwtTokenProvider;
import com.parkpulse.parkingreservation.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ParkingLotRepository parkingLotRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    @DisplayName("Should successfully register customer with hashed password and return JWT")
    void testRegisterCustomer_Success() {
        RegisterRequest request = new RegisterRequest("Bob Driver", "bob@example.com", "secret123", "+1-555-4321");

        when(userRepository.existsByEmail("bob@example.com")).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("bcrypt-hashed-secret");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(77L);
            return u;
        });
        when(tokenProvider.generateToken("bob@example.com", Role.ROLE_CUSTOMER, 77L)).thenReturn("mock-jwt-token");

        AuthResponse response = authService.registerCustomer(request);

        assertNotNull(response);
        assertEquals("mock-jwt-token", response.getToken());
        assertEquals("bob@example.com", response.getEmail());
        assertEquals(Role.ROLE_CUSTOMER, response.getRole());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Should reject customer registration when email already exists")
    void testRegisterCustomer_DuplicateEmail_ThrowsBadRequestException() {
        RegisterRequest request = new RegisterRequest("Bob Driver", "bob@example.com", "secret123", "+1-555-4321");

        when(userRepository.existsByEmail("bob@example.com")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> authService.registerCustomer(request));
        verify(userRepository, never()).save(any(User.class));
    }
}
