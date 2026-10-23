-- ========================================================
-- ParkPulse: Smart Parking Slot Reservation System
-- Sample Database Seed Script (data.sql)
-- ========================================================

-- 1. Insert Default Amenities
INSERT IGNORE INTO amenities (id, name, icon_code, description) VALUES
(1, 'EV Fast Charger', 'zap', 'High-speed 50kW DC fast charging station'),
(2, 'Covered Roof', 'umbrella', 'Sheltered from rain and direct sunlight'),
(3, 'Handicap Accessible', 'accessibility', 'Wide bay near elevator and wheelchair ramps'),
(4, 'CCTV Surveillance', 'shield', '24/7 high-definition camera monitoring'),
(5, 'Wide SUV Bay', 'maximize', 'Extra wide clearance for full-size SUVs');

-- 2. Insert Default Roles & Users (BCrypt hash for 'password123')
-- $2a$10$e8w.p886t6E9l02v7h7iC.Xh9F7r1t9T0e7B8w2Z4x6Y8u0i2O4qS
INSERT IGNORE INTO users (id, full_name, email, password, phone, role, created_at) VALUES
(1, 'System Administrator', 'admin@parkpulse.com', '$2a$10$wK1W2i2Rkm63J0m6t.9q3e56E25eI5Lh84fI9w5aC04yXgR5jB5mO', '+1-555-0100', 'ROLE_ADMIN', NOW()),
(2, 'Mall Lot Manager', 'manager.mall@parkpulse.com', '$2a$10$wK1W2i2Rkm63J0m6t.9q3e56E25eI5Lh84fI9w5aC04yXgR5jB5mO', '+1-555-0200', 'ROLE_MANAGER', NOW()),
(3, 'John Doe Customer', 'customer@parkpulse.com', '$2a$10$wK1W2i2Rkm63J0m6t.9q3e56E25eI5Lh84fI9w5aC04yXgR5jB5mO', '+1-555-0300', 'ROLE_CUSTOMER', NOW());

-- 3. Insert Parking Lots (Malls, Campus, Airport Deck)
INSERT IGNORE INTO parking_lots (id, name, location_type, address, total_floors, total_capacity, is_active, manager_id) VALUES
(1, 'Nexus Grand Mall - East Deck', 'MALL', '800 Innovation Blvd, City Center', 3, 36, true, 2),
(2, 'Tech University Central Campus', 'CAMPUS', '100 University Avenue, North Quad', 2, 24, true, 2),
(3, 'Metro Corporate Tech Park', 'COMPANY', '45 Financial District Way', 2, 20, true, 2);

-- 4. Assign Amenities to Parking Lots (Many-to-Many join table)
INSERT IGNORE INTO parking_lot_amenities (lot_id, amenity_id) VALUES
(1, 1), (1, 2), (1, 3), (1, 4), (1, 5),
(2, 1), (2, 2), (2, 3), (2, 4),
(3, 1), (3, 2), (3, 4);

-- 5. Insert Parking Slots for Lot 1 (Floor 1: B1, Floor 2: Ground, Floor 3: L1)
-- Floor 1 (Basement 1) - Regular, EV, and Handicapped
INSERT IGNORE INTO parking_slots (id, slot_number, floor_number, row_number, col_number, slot_type, base_hourly_rate, status, lot_id) VALUES
(1, 'B1-01', 1, 1, 1, 'EV_CHARGING', 6.00, 'AVAILABLE', 1),
(2, 'B1-02', 1, 1, 2, 'EV_CHARGING', 6.00, 'AVAILABLE', 1),
(3, 'B1-03', 1, 1, 3, 'HANDICAPPED', 4.00, 'AVAILABLE', 1),
(4, 'B1-04', 1, 1, 4, 'REGULAR', 4.00, 'AVAILABLE', 1),
(5, 'B1-05', 1, 1, 5, 'REGULAR', 4.00, 'AVAILABLE', 1),
(6, 'B1-06', 1, 1, 6, 'REGULAR', 4.00, 'AVAILABLE', 1),
(7, 'B1-07', 1, 2, 1, 'REGULAR', 4.00, 'AVAILABLE', 1),
(8, 'B1-08', 1, 2, 2, 'REGULAR', 4.00, 'AVAILABLE', 1),
(9, 'B1-09', 1, 2, 3, 'REGULAR', 4.00, 'AVAILABLE', 1),
(10, 'B1-10', 1, 2, 4, 'VIP', 8.00, 'AVAILABLE', 1),
(11, 'B1-11', 1, 2, 5, 'VIP', 8.00, 'AVAILABLE', 1),
(12, 'B1-12', 1, 2, 6, 'REGULAR', 4.00, 'AVAILABLE', 1);

-- Floor 2 (Ground Floor)
INSERT IGNORE INTO parking_slots (id, slot_number, floor_number, row_number, col_number, slot_type, base_hourly_rate, status, lot_id) VALUES
(13, 'G-01', 2, 1, 1, 'EV_CHARGING', 6.00, 'AVAILABLE', 1),
(14, 'G-02', 2, 1, 2, 'REGULAR', 4.00, 'AVAILABLE', 1),
(15, 'G-03', 2, 1, 3, 'HANDICAPPED', 4.00, 'AVAILABLE', 1),
(16, 'G-04', 2, 1, 4, 'REGULAR', 4.00, 'AVAILABLE', 1),
(17, 'G-05', 2, 1, 5, 'REGULAR', 4.00, 'AVAILABLE', 1),
(18, 'G-06', 2, 1, 6, 'REGULAR', 4.00, 'AVAILABLE', 1),
(19, 'G-07', 2, 2, 1, 'REGULAR', 4.00, 'AVAILABLE', 1),
(20, 'G-08', 2, 2, 2, 'REGULAR', 4.00, 'AVAILABLE', 1),
(21, 'G-09', 2, 2, 3, 'VIP', 8.00, 'AVAILABLE', 1),
(22, 'G-10', 2, 2, 4, 'VIP', 8.00, 'AVAILABLE', 1),
(23, 'G-11', 2, 2, 5, 'REGULAR', 4.00, 'AVAILABLE', 1),
(24, 'G-12', 2, 2, 6, 'REGULAR', 4.00, 'AVAILABLE', 1);

-- Floor 3 (Level 1)
INSERT IGNORE INTO parking_slots (id, slot_number, floor_number, row_number, col_number, slot_type, base_hourly_rate, status, lot_id) VALUES
(25, 'L1-01', 3, 1, 1, 'REGULAR', 3.50, 'AVAILABLE', 1),
(26, 'L1-02', 3, 1, 2, 'REGULAR', 3.50, 'AVAILABLE', 1),
(27, 'L1-03', 3, 1, 3, 'REGULAR', 3.50, 'AVAILABLE', 1),
(28, 'L1-04', 3, 1, 4, 'REGULAR', 3.50, 'AVAILABLE', 1),
(29, 'L1-05', 3, 1, 5, 'REGULAR', 3.50, 'AVAILABLE', 1),
(30, 'L1-06', 3, 1, 6, 'REGULAR', 3.50, 'AVAILABLE', 1),
(31, 'L1-07', 3, 2, 1, 'REGULAR', 3.50, 'AVAILABLE', 1),
(32, 'L1-08', 3, 2, 2, 'REGULAR', 3.50, 'AVAILABLE', 1),
(33, 'L1-09', 3, 2, 3, 'REGULAR', 3.50, 'AVAILABLE', 1),
(34, 'L1-10', 3, 2, 4, 'REGULAR', 3.50, 'AVAILABLE', 1),
(35, 'L1-11', 3, 2, 5, 'REGULAR', 3.50, 'AVAILABLE', 1),
(36, 'L1-12', 3, 2, 6, 'REGULAR', 3.50, 'AVAILABLE', 1);

-- 6. Insert Sample Customer Vehicle
INSERT IGNORE INTO vehicles (id, license_plate, vehicle_type, model, user_id) VALUES
(1, 'ABC-1234', 'SEDAN', 'Toyota Camry', 3),
(2, 'EV-8899', 'EV', 'Tesla Model 3', 3);
