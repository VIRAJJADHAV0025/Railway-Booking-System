# Railway Reservation System

A console-based railway reservation project built with Java 26, Hibernate 6 and MySQL.

## Project structure

- `src/main/java/com/railway/` contains the Java source code.
- `pom.xml` contains the Maven dependencies and build settings.
- The SQL setup script is provided separately as `Railway_booking_Database.sql`.

The Java project does not create the database, create tables or insert sample records. The SQL script is responsible for the initial database setup. After setup, the application reads and updates data in MySQL.

## 1. Create the database

1. Open MySQL Workbench and connect to your MySQL server.
2. Open `Railway_Booking_Database.sql`.
3. Execute the entire script.
4. Refresh the Schemas panel and confirm that `railway` and its tables are present.

Run the setup script only once on a fresh database. It creates the tables and inserts the sample records, including the administrator account.

## 2. Configure the MySQL connection

Open `src/main/java/com/railway/HibernateUtil.java` and check the default connection settings:

- Database: `railway`
- Host: `localhost`
- Port: `3306`
- Username: `root`
- Password: `root`

Change the username and password to match your MySQL installation. You can also provide the connection details through these environment variables:

- `DB_URL`
- `DB_USER`
- `DB_PASSWORD`

## 3. Run the application

Requirements: JDK 17 or later, Maven and MySQL Server.

Open a terminal in the project folder, where `pom.xml` is located, and run:

```bash
mvn clean compile
mvn exec:java
```

## Administrator login

- Username: `admin`
- Password: `admin123`

The administrator account is inserted by the SQL script. The Java application does not create it.

## Main features

- Passenger registration and login
- Train search by source station, destination station and journey date
- Train route and timetable display
- Coach-class and seat-availability display
- Booking for up to six passengers
- PNR generation
- Passenger-specific booking history
- Booking cancellation
- Administrator view of all bookings

## Notes and limitations

- Hibernate uses `validate` mode. It checks that the database tables match the Java entity mappings; it does not create or update the schema.
- The sample train routes, timings, running days and seat capacities are demonstration data. They are not verified live railway schedules.
- This project is not connected to IRCTC or the Indian Railways reservation system. It cannot issue official railway tickets.
- Passwords are stored as plain text for this academic demonstration. This is not suitable for a real application.
- Seat availability is calculated for the whole train and class on a journey date. The project does not allocate specific berths or track availability separately for each route segment.
