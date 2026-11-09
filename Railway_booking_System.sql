-- Railway Reservation System
-- Database setup script
--
-- Run this script in MySQL Workbench to create the database, tables and sample records.
-- If railway_db already exists, the script will use that database.
-- Run this script only once on a fresh database. Running it again will create
-- duplicate-key errors because the sample records are already present.

-- DROP DATABASE IF EXISTS railway;

CREATE DATABASE IF NOT EXISTS railway;

USE railway;


-- Stores login details for passengers and administrators.
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    role VARCHAR(10) NOT NULL DEFAULT 'PASSENGER',
    full_name VARCHAR(100),
    email VARCHAR(100) UNIQUE,
    phone VARCHAR(15),
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
);

SELECT * FROM users;

-- Adds the administrator account used to open the admin menu.
-- This sample password is for demonstration purposes only.
INSERT INTO users (username, password, role, full_name)
VALUES ('admin', 'admin123', 'ADMIN', 'System Admin');


-- Stores the names, station codes and cities of railway stations.
CREATE TABLE stations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(10) UNIQUE,
    name VARCHAR(100) NOT NULL UNIQUE,
    city VARCHAR(100)
);


-- Stores the basic details of each train.
CREATE TABLE trains (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    train_number VARCHAR(10) NOT NULL UNIQUE,
    train_name VARCHAR(100) NOT NULL
);


-- Stores each train route, including the order and timing of its stops.
CREATE TABLE train_stops (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    train_id BIGINT NOT NULL,
    station_id BIGINT NOT NULL,
    stop_order INT NOT NULL,
    arrival_time TIME,
    departure_time TIME,
    day_offset INT NOT NULL DEFAULT 0,

    UNIQUE (train_id, stop_order),
    UNIQUE (train_id, station_id),

    FOREIGN KEY (train_id) REFERENCES trains(id),
    FOREIGN KEY (station_id) REFERENCES stations(id)
);


-- Stores the days of the week on which each train operates.
CREATE TABLE train_running_days (
    train_id BIGINT NOT NULL,
    day_of_week VARCHAR(10) NOT NULL,

    PRIMARY KEY (train_id, day_of_week),
    FOREIGN KEY (train_id) REFERENCES trains(id)
);


-- Lists the coach classes available for booking.
CREATE TABLE coach_classes (
    code VARCHAR(5) PRIMARY KEY,
    name VARCHAR(50) NOT NULL
);


-- Adds the coach classes used in this demonstration project.
INSERT INTO coach_classes (code, name)
VALUES
    ('GEN', 'General'),
    ('1A', 'First AC'),
    ('2A', 'Second AC'),
    ('3A', 'Third AC'),
    ('SL', 'Sleeper'),
    ('CC', 'AC Chair Car'),
    ('2S', 'Second Sitting');


-- Stores the available coach classes and seat capacity for each train.
CREATE TABLE train_classes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    train_id BIGINT NOT NULL,
    class_code VARCHAR(5) NOT NULL,
    total_seats INT NOT NULL,

    UNIQUE (train_id, class_code),

    FOREIGN KEY (train_id) REFERENCES trains(id),
    FOREIGN KEY (class_code) REFERENCES coach_classes(code)
);


-- Stores booking details for each passenger journey.
CREATE TABLE bookings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    pnr VARCHAR(10) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    train_id BIGINT NOT NULL,
    class_code VARCHAR(5) NOT NULL,
    journey_date DATE NOT NULL,
    seat_count INT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'CONFIRMED',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    CHECK (seat_count BETWEEN 1 AND 6),
    INDEX idx_seats (train_id, class_code, journey_date, status),

    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (train_id) REFERENCES trains(id),
    FOREIGN KEY (class_code) REFERENCES coach_classes(code)
);


-- Stores the passenger details linked to each booking.
CREATE TABLE booking_passengers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    booking_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    age INT NOT NULL,
    gender CHAR(1) NOT NULL,
    seat_label VARCHAR(10),

    FOREIGN KEY (booking_id) REFERENCES bookings(id)
);


-- ------------------------------------------------------------
-- SAMPLE DATA
-- The train names and timings below are for project demonstration.
-- They are not verified live Indian Railways schedules.
-- ------------------------------------------------------------

-- Add the sample railway stations.
INSERT INTO stations (code, name, city)
VALUES
    ('CSMT', 'Chhatrapati Shivaji Maharaj Terminus', 'Mumbai'),
    ('DR', 'Dadar', 'Mumbai'),
    ('TNA', 'Thane', 'Thane'),
    ('KYN', 'Kalyan Junction', 'Kalyan'),
    ('LNL', 'Lonavala', 'Lonavala'),
    ('PUNE', 'Pune Junction', 'Pune'),
    ('NGP', 'Nagpur Junction', 'Nagpur'),
    ('G', 'Gondia Junction', 'Gondia'),
    ('ST', 'Surat', 'Surat'),
    ('ADI', 'Ahmedabad Junction', 'Ahmedabad');


-- Add the sample trains.
INSERT INTO trains (train_number, train_name)
VALUES
    ('12123', 'Deccan Queen (demo data)'),
    ('12105', 'Vidarbha Express (demo data)'),
    ('12009', 'Shatabdi Express (demo data)');


-- Add the sample route and timetable for the Deccan Queen.
INSERT INTO train_stops
    (train_id, station_id, stop_order, arrival_time, departure_time, day_offset)
SELECT
    t.id, s.id, route.stop_order, route.arrival_time, route.departure_time, route.day_offset
FROM (
    SELECT '12123' AS train_number, 'CSMT' AS station_code, 1 AS stop_order,
           NULL AS arrival_time, '17:10:00' AS departure_time, 0 AS day_offset
    UNION ALL SELECT '12123', 'DR',   2, '17:20:00', '17:22:00', 0
    UNION ALL SELECT '12123', 'TNA',  3, '17:42:00', '17:44:00', 0
    UNION ALL SELECT '12123', 'KYN',  4, '18:00:00', '18:02:00', 0
    UNION ALL SELECT '12123', 'LNL',  5, '19:20:00', '19:22:00', 0
    UNION ALL SELECT '12123', 'PUNE', 6, '20:25:00', NULL,        0
) AS route
JOIN trains t ON t.train_number = route.train_number
JOIN stations s ON s.code = route.station_code;


-- Add the sample route and timetable for the Vidarbha Express.
INSERT INTO train_stops
    (train_id, station_id, stop_order, arrival_time, departure_time, day_offset)
SELECT
    t.id, s.id, route.stop_order, route.arrival_time, route.departure_time, route.day_offset
FROM (
    SELECT '12105' AS train_number, 'CSMT' AS station_code, 1 AS stop_order,
           NULL AS arrival_time, '19:10:00' AS departure_time, 0 AS day_offset
    UNION ALL SELECT '12105', 'KYN',  2, '20:05:00', '20:07:00', 0
    UNION ALL SELECT '12105', 'NGP',  3, '08:00:00', '08:10:00', 1
    UNION ALL SELECT '12105', 'G',    4, '10:15:00', NULL,        1
) AS route
JOIN trains t ON t.train_number = route.train_number
JOIN stations s ON s.code = route.station_code;


-- Add the sample route and timetable for the Shatabdi Express.
INSERT INTO train_stops
    (train_id, station_id, stop_order, arrival_time, departure_time, day_offset)
SELECT
    t.id, s.id, route.stop_order, route.arrival_time, route.departure_time, route.day_offset
FROM (
    SELECT '12009' AS train_number, 'CSMT' AS station_code, 1 AS stop_order,
           NULL AS arrival_time, '06:00:00' AS departure_time, 0 AS day_offset
    UNION ALL SELECT '12009', 'ST',  2, '09:00:00', '09:02:00', 0
    UNION ALL SELECT '12009', 'ADI', 3, '11:15:00', NULL,        0
) AS route
JOIN trains t ON t.train_number = route.train_number
JOIN stations s ON s.code = route.station_code;


-- In this demonstration project, all sample trains are marked as running daily.
INSERT INTO train_running_days (train_id, day_of_week)
SELECT t.id, days.day_of_week
FROM trains t
CROSS JOIN (
    SELECT 'MONDAY' AS day_of_week
    UNION ALL SELECT 'TUESDAY'
    UNION ALL SELECT 'WEDNESDAY'
    UNION ALL SELECT 'THURSDAY'
    UNION ALL SELECT 'FRIDAY'
    UNION ALL SELECT 'SATURDAY'
    UNION ALL SELECT 'SUNDAY'
) AS days
WHERE t.train_number IN ('12123', '12105', '12009');


-- Add the sample coach classes and seat capacities for each train.
INSERT INTO train_classes (train_id, class_code, total_seats)
SELECT t.id, sample.class_code, sample.total_seats
FROM (
    SELECT '12123' AS train_number, 'CC' AS class_code, 80 AS total_seats
    UNION ALL SELECT '12123', '2S', 120
    UNION ALL SELECT '12105', 'SL', 300
    UNION ALL SELECT '12105', '3A', 72
    UNION ALL SELECT '12009', 'CC', 90
) AS sample
JOIN trains t ON t.train_number = sample.train_number;


-- Administrator login details created by this script:
-- Username: admin
-- Password: admin123

-- ------------------------------------------------------------
-- EXTRA SAMPLE TRAIN: Dadar -> Surat -> Ahmedabad (demo data)
-- ------------------------------------------------------------

INSERT INTO trains (train_number, train_name)
VALUES ('12901', 'Gujarat Mail (demo data)');

INSERT INTO train_stops
    (train_id, station_id, stop_order, arrival_time, departure_time, day_offset)
SELECT
    t.id, s.id, route.stop_order, route.arrival_time, route.departure_time, route.day_offset
FROM (
    SELECT '12901' AS train_number, 'DR' AS station_code, 1 AS stop_order,
           NULL AS arrival_time, '22:10:00' AS departure_time, 0 AS day_offset
    UNION ALL SELECT '12901', 'ST',  2, '03:20:00', '03:25:00', 1
    UNION ALL SELECT '12901', 'ADI', 3, '06:15:00', NULL,        1
) AS route
JOIN trains t ON t.train_number = route.train_number
JOIN stations s ON s.code = route.station_code;

INSERT INTO train_running_days (train_id, day_of_week)
SELECT t.id, days.day_of_week
FROM trains t
CROSS JOIN (
    SELECT 'MONDAY' AS day_of_week
    UNION ALL SELECT 'TUESDAY'
    UNION ALL SELECT 'WEDNESDAY'
    UNION ALL SELECT 'THURSDAY'
    UNION ALL SELECT 'FRIDAY'
    UNION ALL SELECT 'SATURDAY'
    UNION ALL SELECT 'SUNDAY'
) AS days
WHERE t.train_number = '12901';

INSERT INTO train_classes (train_id, class_code, total_seats)
SELECT t.id, sample.class_code, sample.total_seats
FROM (
    SELECT '12901' AS train_number, 'SL' AS class_code, 200 AS total_seats
    UNION ALL SELECT '12901', '3A', 72
    UNION ALL SELECT '12901', '2A', 48
) AS sample
JOIN trains t ON t.train_number = sample.train_number;
