-- ========================================================
-- ParkPulse: Smart Parking Slot Reservation System
-- Database DDL Schema (schema.sql)
-- ========================================================

CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    phone VARCHAR(30),
    role VARCHAR(30) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS amenities (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    icon_code VARCHAR(50),
    description TEXT
);

CREATE TABLE IF NOT EXISTS parking_lots (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    location_type VARCHAR(30) NOT NULL,
    address VARCHAR(255) NOT NULL,
    total_floors INT NOT NULL DEFAULT 1,
    total_capacity INT NOT NULL DEFAULT 0,
    image_url VARCHAR(500),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    manager_id BIGINT,
    FOREIGN KEY (manager_id) REFERENCES users(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS parking_lot_amenities (
    lot_id BIGINT NOT NULL,
    amenity_id BIGINT NOT NULL,
    PRIMARY KEY (lot_id, amenity_id),
    FOREIGN KEY (lot_id) REFERENCES parking_lots(id) ON DELETE CASCADE,
    FOREIGN KEY (amenity_id) REFERENCES amenities(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS parking_slots (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    slot_number VARCHAR(20) NOT NULL,
    floor_number INT NOT NULL DEFAULT 1,
    row_number INT DEFAULT 1,
    col_number INT DEFAULT 1,
    slot_type VARCHAR(30) NOT NULL DEFAULT 'REGULAR',
    base_hourly_rate DECIMAL(10,2) NOT NULL DEFAULT 5.00,
    status VARCHAR(30) NOT NULL DEFAULT 'AVAILABLE',
    lot_id BIGINT NOT NULL,
    UNIQUE KEY uk_lot_slot (lot_id, slot_number),
    FOREIGN KEY (lot_id) REFERENCES parking_lots(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS vehicles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    license_plate VARCHAR(30) NOT NULL UNIQUE,
    vehicle_type VARCHAR(30) NOT NULL DEFAULT 'SEDAN',
    model VARCHAR(100),
    user_id BIGINT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS reservations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    reservation_code VARCHAR(32) NOT NULL UNIQUE,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    actual_check_in DATETIME,
    actual_check_out DATETIME,
    base_fee DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    penalty_fee DECIMAL(10,2) DEFAULT 0.00,
    total_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    overstay_minutes BIGINT DEFAULT 0,
    status VARCHAR(30) NOT NULL DEFAULT 'CONFIRMED',
    user_id BIGINT NOT NULL,
    slot_id BIGINT NOT NULL,
    vehicle_id BIGINT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (slot_id) REFERENCES parking_slots(id) ON DELETE CASCADE,
    FOREIGN KEY (vehicle_id) REFERENCES vehicles(id) ON DELETE CASCADE
);
