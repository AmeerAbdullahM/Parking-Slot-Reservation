package com.parkpulse.parkingreservation.config;

import com.parkpulse.parkingreservation.model.*;
import com.parkpulse.parkingreservation.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final ParkingLotRepository parkingLotRepository;
    private final ParkingSlotRepository parkingSlotRepository;
    private final AmenityRepository amenityRepository;
    private final VehicleRepository vehicleRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           ParkingLotRepository parkingLotRepository,
                           ParkingSlotRepository parkingSlotRepository,
                           AmenityRepository amenityRepository,
                           VehicleRepository vehicleRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.parkingLotRepository = parkingLotRepository;
        this.parkingSlotRepository = parkingSlotRepository;
        this.amenityRepository = amenityRepository;
        this.vehicleRepository = vehicleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            logger.info("Database already seeded. Skipping initial data generation.");
            return;
        }

        logger.info("Initializing ParkPulse sample database data...");

        // 1. Create Default Amenities
        Amenity ev = amenityRepository.save(new Amenity("EV Fast Charger", "zap", "High-speed 50kW DC fast charging station"));
        Amenity covered = amenityRepository.save(new Amenity("Covered Roof", "umbrella", "Sheltered from rain and direct sunlight"));
        Amenity handicap = amenityRepository.save(new Amenity("Handicap Accessible", "accessibility", "Wide bay near elevator and wheelchair ramps"));
        Amenity cctv = amenityRepository.save(new Amenity("CCTV Surveillance", "shield", "24/7 high-definition security camera monitoring"));
        Amenity suv = amenityRepository.save(new Amenity("Wide SUV Bay", "maximize", "Extra wide clearance for full-size SUVs"));

        Set<Amenity> mallAmenities = new HashSet<>();
        mallAmenities.add(ev);
        mallAmenities.add(covered);
        mallAmenities.add(handicap);
        mallAmenities.add(cctv);
        mallAmenities.add(suv);

        // 2. Create Users for all 3 roles (admin, manager, customer)
        User admin = userRepository.save(new User(
                "System Administrator",
                "admin@parkpulse.com",
                passwordEncoder.encode("password123"),
                "+1-555-0100",
                Role.ROLE_ADMIN
        ));

        User manager = userRepository.save(new User(
                "Nexus Mall Lot Manager",
                "manager.mall@parkpulse.com",
                passwordEncoder.encode("password123"),
                "+1-555-0200",
                Role.ROLE_MANAGER
        ));

        User customer = userRepository.save(new User(
                "John Customer",
                "customer@parkpulse.com",
                passwordEncoder.encode("password123"),
                "+1-555-0300",
                Role.ROLE_CUSTOMER
        ));

        // 3. Create Sample Facilities (Malls & Campus Lots)
        ParkingLot mall = new ParkingLot("Nexus Grand Mall - East Deck", LocationType.MALL, "800 Innovation Blvd, City Center", 3, 36, manager);
        mall.setAmenities(mallAmenities);
        mall.setImageUrl("https://images.unsplash.com/photo-1590674899484-d5640e854abe?w=800&auto=format&fit=crop");
        mall = parkingLotRepository.save(mall);

        ParkingLot campus = new ParkingLot("Tech University North Quad", LocationType.CAMPUS, "100 University Ave, Campus Gate 2", 2, 24, manager);
        campus.setAmenities(new HashSet<>(Set.of(ev, covered, handicap, cctv)));
        campus.setImageUrl("https://images.unsplash.com/photo-1506521781263-d8422e82f27a?w=800&auto=format&fit=crop");
        campus = parkingLotRepository.save(campus);

        // 4. Create 2D Grid Slots for Mall (Floor 1: B1, Floor 2: Ground, Floor 3: L1)
        // Floor 1 (Basement 1) - 2 rows x 6 columns
        for (int row = 1; row <= 2; row++) {
            for (int col = 1; col <= 6; col++) {
                int index = (row - 1) * 6 + col;
                String slotNum = String.format("B1-%02d", index);
                SlotType type = SlotType.REGULAR;
                BigDecimal rate = BigDecimal.valueOf(4.00);

                if (index <= 2) {
                    type = SlotType.EV_CHARGING;
                    rate = BigDecimal.valueOf(6.00);
                } else if (index == 3) {
                    type = SlotType.HANDICAPPED;
                } else if (index >= 10) {
                    type = SlotType.VIP;
                    rate = BigDecimal.valueOf(8.00);
                }

                parkingSlotRepository.save(new ParkingSlot(slotNum, 1, row, col, type, rate, mall));
            }
        }

        // Floor 2 (Ground Floor) - 2 rows x 6 columns
        for (int row = 1; row <= 2; row++) {
            for (int col = 1; col <= 6; col++) {
                int index = (row - 1) * 6 + col;
                String slotNum = String.format("G-%02d", index);
                SlotType type = SlotType.REGULAR;
                BigDecimal rate = BigDecimal.valueOf(5.00);

                if (index == 1) {
                    type = SlotType.EV_CHARGING;
                    rate = BigDecimal.valueOf(7.00);
                } else if (index == 2) {
                    type = SlotType.HANDICAPPED;
                } else if (index >= 9 && index <= 10) {
                    type = SlotType.VIP;
                    rate = BigDecimal.valueOf(9.00);
                }

                parkingSlotRepository.save(new ParkingSlot(slotNum, 2, row, col, type, rate, mall));
            }
        }

        // Floor 3 (Level 1) - 2 rows x 6 columns
        for (int row = 1; row <= 2; row++) {
            for (int col = 1; col <= 6; col++) {
                int index = (row - 1) * 6 + col;
                String slotNum = String.format("L1-%02d", index);
                parkingSlotRepository.save(new ParkingSlot(slotNum, 3, row, col, SlotType.REGULAR, BigDecimal.valueOf(3.50), mall));
            }
        }

        // 5. Create 2D Grid Slots for Campus (Floor 1: 12 slots, Floor 2: 12 slots)
        for (int floor = 1; floor <= 2; floor++) {
            for (int row = 1; row <= 2; row++) {
                for (int col = 1; col <= 6; col++) {
                    int index = (row - 1) * 6 + col;
                    String slotNum = String.format("C%d-%02d", floor, index);
                    SlotType type = (index == 1) ? SlotType.EV_CHARGING : SlotType.REGULAR;
                    BigDecimal rate = (type == SlotType.EV_CHARGING) ? BigDecimal.valueOf(4.50) : BigDecimal.valueOf(2.50);
                    parkingSlotRepository.save(new ParkingSlot(slotNum, floor, row, col, type, rate, campus));
                }
            }
        }

        // 6. Register Customer Vehicles
        vehicleRepository.save(new Vehicle("ABC-1234", VehicleType.SEDAN, "Toyota Camry", customer));
        vehicleRepository.save(new Vehicle("EV-9900", VehicleType.EV, "Tesla Model 3", customer));

        logger.info("ParkPulse database initialization completed successfully with 3 roles and 60 slots.");
    }
}
