-- Run ONCE in MySQL Workbench on your existing railway database.
USE railway;

-- 1. Old test bookings have no stations, so remove them first.
DELETE FROM booking_passengers;
DELETE FROM bookings;

-- 2. Bookings now remember the source and destination station.
ALTER TABLE bookings
    ADD COLUMN from_station_id BIGINT NOT NULL AFTER class_code,
    ADD COLUMN to_station_id BIGINT NOT NULL AFTER from_station_id,
    ADD FOREIGN KEY (from_station_id) REFERENCES stations(id),
    ADD FOREIGN KEY (to_station_id) REFERENCES stations(id);

-- 3. Return train: Ahmedabad -> Surat -> Dadar (demo data)
INSERT INTO trains (train_number, train_name)
VALUES ('12902', 'Gujarat Mail Return (demo data)');

INSERT INTO train_stops
    (train_id, station_id, stop_order, arrival_time, departure_time, day_offset)
SELECT t.id, s.id, route.stop_order, route.arrival_time, route.departure_time, route.day_offset
FROM (
    SELECT '12902' AS train_number, 'ADI' AS station_code, 1 AS stop_order,
           NULL AS arrival_time, '21:00:00' AS departure_time, 0 AS day_offset
    UNION ALL SELECT '12902', 'ST', 2, '01:05:00', '01:10:00', 1
    UNION ALL SELECT '12902', 'DR', 3, '06:30:00', NULL,        1
) AS route
JOIN trains t ON t.train_number = route.train_number
JOIN stations s ON s.code = route.station_code;

INSERT INTO train_running_days (train_id, day_of_week)
SELECT t.id, days.day_of_week
FROM trains t
CROSS JOIN (
    SELECT 'MONDAY' AS day_of_week UNION ALL SELECT 'TUESDAY' UNION ALL SELECT 'WEDNESDAY'
    UNION ALL SELECT 'THURSDAY' UNION ALL SELECT 'FRIDAY' UNION ALL SELECT 'SATURDAY'
    UNION ALL SELECT 'SUNDAY'
) AS days
WHERE t.train_number = '12902';

INSERT INTO train_classes (train_id, class_code, total_seats)
SELECT t.id, sample.class_code, sample.total_seats
FROM (
    SELECT '12902' AS train_number, 'SL' AS class_code, 200 AS total_seats
    UNION ALL SELECT '12902', '3A', 72
    UNION ALL SELECT '12902', '2A', 48
) AS sample
JOIN trains t ON t.train_number = sample.train_number;