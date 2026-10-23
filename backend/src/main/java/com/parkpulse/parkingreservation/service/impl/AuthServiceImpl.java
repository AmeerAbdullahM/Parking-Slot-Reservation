package com.parkpulse.parkingreservation.service.impl;

import com.parkpulse.parkingreservation.dto.AuthRequest;
import com.parkpulse.parkingreservation.dto.AuthResponse;
import com.parkpulse.parkingreservation.dto.CreateManagerRequest;
import com.parkpulse.parkingreservation.dto.RegisterRequest;
import com.parkpulse.parkingreservation.exception.BadRequestException;
import com.parkpulse.parkingreservation.exception.ResourceNotFoundException;
import com.parkpulse.parkingreservation.model.ParkingLot;
import com.parkpulse.parkingreservation.model.Role;
import com.parkpulse.parkingreservation.model.User;
import com.parkpulse.parkingreservation.repository.ParkingLotRepository;
import com.parkpulse.parkingreservation.repository.UserRepository;
import com.parkpulse.parkingreservation.security.JwtTokenProvider;
import com.parkpulse.parkingreservation.service.AuthService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final ParkingLotRepository parkingLotRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final AuthenticationManager authenticationManager;

    public AuthServiceImpl(UserRepository userRepository,
                           ParkingLotRepository parkingLotRepository,
                           PasswordEncoder passwordEncoder,
                           JwtTokenProvider tokenProvider,
                           AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.parkingLotRepository = parkingLotRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.authenticationManager = authenticationManager;
    }

    @Override
    @Transactional
    public AuthResponse registerCustomer(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail().trim().toLowerCase())) {
            throw new BadRequestException("Email is already registered: " + request.getEmail());
        }

        User user = new User(
                request.getFullName().trim(),
                request.getEmail().trim().toLowerCase(),
                passwordEncoder.encode(request.getPassword()),
                request.getPhone() != null ? request.getPhone().trim() : null,
                Role.ROLE_CUSTOMER
        );

        User savedUser = userRepository.save(user);
        String token = tokenProvider.generateToken(savedUser.getEmail(), savedUser.getRole(), savedUser.getId());

        return new AuthResponse(token, savedUser.getId(), savedUser.getFullName(), savedUser.getEmail(), savedUser.getRole(), null);
    }

    @Override
    @Transactional
    public AuthResponse createManager(CreateManagerRequest request) {
        if (userRepository.existsByEmail(request.getEmail().trim().toLowerCase())) {
            throw new BadRequestException("Manager email is already registered: " + request.getEmail());
        }

        User manager = new User(
                request.getFullName().trim(),
                request.getEmail().trim().toLowerCase(),
                passwordEncoder.encode(request.getPassword()),
                request.getPhone() != null ? request.getPhone().trim() : null,
                Role.ROLE_MANAGER
        );

        User savedManager = userRepository.save(manager);

        Long assignedLotId = null;
        if (request.getAssignedLotId() != null) {
            ParkingLot lot = parkingLotRepository.findById(request.getAssignedLotId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parking Lot not found with ID: " + request.getAssignedLotId()));
            lot.setManager(savedManager);
            parkingLotRepository.save(lot);
            assignedLotId = lot.getId();
        }

        String token = tokenProvider.generateToken(savedManager.getEmail(), savedManager.getRole(), savedManager.getId());

        return new AuthResponse(token, savedManager.getId(), savedManager.getFullName(), savedManager.getEmail(), savedManager.getRole(), assignedLotId);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(AuthRequest request) {
        String email = request.getEmail().trim().toLowerCase();

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        String token = tokenProvider.generateToken(authentication);

        Long assignedLotId = null;
        if (user.getRole() == Role.ROLE_MANAGER) {
            List<ParkingLot> managedLots = parkingLotRepository.findByManagerId(user.getId());
            if (!managedLots.isEmpty()) {
                assignedLotId = managedLots.get(0).getId();
            }
        }

        return new AuthResponse(token, user.getId(), user.getFullName(), user.getEmail(), user.getRole(), assignedLotId);
    }

    @Override
    @Transactional(readOnly = true)
    public User getCurrentAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new BadRequestException("No authenticated user found in session");
        }

        String email = auth.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found with email: " + email));
    }
}
