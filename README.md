# Railway Booking System: Java + Hibernate + MySQL

## What this version does
- Real train examples: 12123 Deccan Queen (daily) and 12009 Shatabdi Express (Mon-Sat). Seat counts are invented demo values.
- Trains have running days. Only trains running on the chosen weekday are offered.
- Booking window: today to today + 60 days (simplified student rule, not the official PRS calculation).
- **Seats are tracked per train per date.** Seats left = train's total seats - confirmed bookings on that date.
- **Booking is locked per train** (row lock inside a transaction), so two users cannot take the same last seats.
- Max 6 seats per booking.
- Passengers can cancel their own future bookings. Seats become free again.
- Admin input is validated (running days, duplicate train numbers, blank fields). Bad input does not log the admin out.
- Clear startup error if the database cannot be reached or `DB_PASSWORD` is missing.

## Run
1. Create an empty MySQL database (default name `Railwaydb`).
2. Set the DB password as an environment variable. It is NOT stored in code.
   - Windows PowerShell: `$env:DB_PASSWORD="your_password"`
   - Windows cmd: `set DB_PASSWORD=your_password`
   - Linux/macOS: `export DB_PASSWORD=your_password`
   - Optional: `DB_URL` (default `jdbc:mysql://localhost:3306/Railwaydb`) and `DB_USER` (default `root`).
3. From the folder with `pom.xml`:

       mvn clean compile
       mvn exec:java

Demo admin: admin / admin123. Register a passenger account from the menu before booking.

## IMPORTANT: use a fresh database
This version removes `availableSeats` from `trains` and adds `status` and `createdAt` to `bookings`.
Hibernate's `update` mode does not drop old columns or fill new ones for old rows, so inserts will fail on an old schema.
Drop and recreate the database (or use a new one). Back up anything you need first.

## Security notes
- If an earlier version of this project (with the hardcoded DB password) was ever pushed to GitHub, change that MySQL password now. Removing it from the code does not remove it from git history.
- Passwords are still stored as plain text, and the admin login is still `admin/admin123`. This is a known gap. Do not use real passwords.

## Limitations
- Not connected to IRCTC/PRS. No live data, no official confirmation.
- No coach/seat numbers, no passenger names per seat, no departure times.
- `hbm2ddl.auto=update` is for demos only.
