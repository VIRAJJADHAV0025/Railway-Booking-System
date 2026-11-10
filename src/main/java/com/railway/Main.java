package com.railway;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);

    // Passenger details kept as plain data so the same people can travel on the return ticket.
    private record PassengerInfo(String name, int age, String gender) {
    }

    public static void main(String[] args) {
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "warn");

        BookingService bookingService = new BookingService();

        try {
            showMainMenu(bookingService);
        } catch (Exception exception) {
            System.out.println("Something went wrong: " + exception.getMessage());
        } finally {
            HibernateUtil.shutdown();
            scanner.close();
        }
    }

    private static void showMainMenu(BookingService bookingService) {
        boolean running = true;

        while (running) {
            System.out.println("\n===== RAILWAY RESERVATION SYSTEM =====");
            System.out.println("1. Register as a passenger");
            System.out.println("2. Passenger login");
            System.out.println("3. Admin login");
            System.out.println("4. Exit");
            System.out.print("Enter your choice: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> registerPassenger(bookingService);
                case "2" -> passengerLogin(bookingService);
                case "3" -> adminLogin(bookingService);
                case "4" -> running = false;
                default -> System.out.println("Please enter a valid option.");
            }
        }

        System.out.println("Thank you for using the Railway Reservation System.");
    }

    private static void registerPassenger(BookingService bookingService) {
        System.out.println("\n===== PASSENGER REGISTRATION =====");

        System.out.print("Enter your full name: ");
        String name = scanner.nextLine().trim();

        System.out.print("Choose a username: ");
        String username = scanner.nextLine().trim();

        System.out.print("Choose a password: ");
        String password = scanner.nextLine();

        try {
              boolean registered = bookingService.register(username, password, name);

              if (registered) {
                     System.out.println("Your account has been created successfully.");
              } else {
                     System.out.println("That username is already in use. Please choose another one.");
              }
            } 
        catch (IllegalArgumentException exception) {
                 System.out.println(exception.getMessage());
           }
    }

    private static void passengerLogin(BookingService bookingService) {
        System.out.println("\n===== PASSENGER LOGIN =====");

        System.out.print("Username: ");
        String username = scanner.nextLine().trim();

        System.out.print("Password: ");
        String password = scanner.nextLine();

        User passenger = bookingService.login(username, password, UserRole.PASSENGER);

        if (passenger == null) {
            System.out.println("Login failed. Please check your username and password.");
            return;
        }

        showPassengerMenu(bookingService, passenger);
    }

    private static void showPassengerMenu(BookingService bookingService, User passenger) {
        boolean loggedIn = true;

        while (loggedIn) {
            System.out.println("\n===== PASSENGER MENU =====");
            System.out.println("Welcome, " + passenger.getUsername() + "!");
            System.out.println("1. Search trains and book tickets");
            System.out.println("2. View my bookings");
            System.out.println("3. Cancel a booking");
            System.out.println("4. Logout");
            System.out.print("Enter your choice: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> searchAndBook(bookingService, passenger);
                case "2" -> showPassengerBookings(bookingService, passenger);
                case "3" -> cancelBooking(bookingService, passenger);
                case "4" -> loggedIn = false;
                default -> System.out.println("Please enter a valid option.");
            }
        }
    }

    private static void showPassengerBookings(BookingService bookingService, User passenger) {
        List<Booking> bookings = bookingService.mine(passenger.getId());

        if (bookings.isEmpty()) {
            System.out.println("You have not made any bookings yet.");
            return;
        }

        System.out.println("\n===== MY BOOKINGS =====");
        for (Booking booking : bookings) {
            printTicket(bookingService, booking);
        }
    }

    private static void cancelBooking(BookingService bookingService, User passenger) {
        System.out.print("Enter the PNR of the booking you want to cancel: ");
        String pnr = scanner.nextLine().trim().toUpperCase();

        boolean cancelled = bookingService.cancel(passenger, pnr);

        if (cancelled) {
            System.out.println("Your booking has been cancelled.");
        } else {
            System.out.println("No active booking was found for the given PNR.");
        }
    }

    private static void searchAndBook(BookingService bookingService, User passenger) {
        System.out.println("\n===== SEARCH TRAINS =====");
        System.out.println("Bookings are available from "
                + bookingService.startDate() + " to " + bookingService.endDate() + ".");

        System.out.println("\nAvailable stations:");
        bookingService.stations().forEach(System.out::println);

        System.out.print("Enter the source station code: ");
        String fromCode = scanner.nextLine().trim().toUpperCase();

        System.out.print("Enter the destination station code: ");
        String toCode = scanner.nextLine().trim().toUpperCase();

        System.out.print("Enter the journey date (YYYY-MM-DD): ");
        LocalDate journeyDate;
        try {
            journeyDate = LocalDate.parse(scanner.nextLine().trim());
        } catch (DateTimeParseException e) {
            System.out.println("Please enter the date as YYYY-MM-DD.");
            return;
        }

        List<PassengerInfo> travellers = new ArrayList<>();
        String pnr = bookJourney(bookingService, passenger, fromCode, toCode, journeyDate, travellers);

        if (pnr == null) {
            return;
        }

        System.out.println("\nYour booking has been confirmed.");
        System.out.println("Your PNR number is: " + pnr);

        System.out.print("\nDo you also want a return ticket (" + toCode + " to " + fromCode + ")? (Y/N): ");
        if (!scanner.nextLine().trim().equalsIgnoreCase("Y")) {
            return;
        }

        System.out.print("Enter the return date (YYYY-MM-DD): ");
        LocalDate returnDate;
        try {
            returnDate = LocalDate.parse(scanner.nextLine().trim());
        } catch (DateTimeParseException e) {
            System.out.println("Please enter the date as YYYY-MM-DD.");
            return;
        }

        if (returnDate.isBefore(journeyDate)) {
            System.out.println("The return date cannot be before the journey date.");
            return;
        }

        System.out.println("\n===== RETURN JOURNEY: " + toCode + " to " + fromCode + " =====");
        String returnPnr = bookJourney(bookingService, passenger, toCode, fromCode, returnDate, travellers);

        if (returnPnr != null) {
            System.out.println("\nYour return booking has been confirmed.");
            System.out.println("Your return PNR number is: " + returnPnr);
        }
    }

    // Searches, lets the user pick a train and class, and books it.
    // Returns the PNR, or null if nothing was booked.
    // If "travellers" is empty it asks for passenger details; otherwise it reuses them (return ticket).
    private static String bookJourney(BookingService bookingService, User passenger,
                                      String fromCode, String toCode, LocalDate journeyDate,
                                      List<PassengerInfo> travellers) {
        List<Train> trains;
        try {
            trains = bookingService.search(fromCode, toCode, journeyDate);
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
            return null;
        }

        if (trains.isEmpty()) {
            showOtherDates(bookingService, fromCode, toCode);
            return null;
        }

        System.out.println("\n===== AVAILABLE TRAINS =====");
        for (Train train : trains) {
            printTrainSummary(bookingService, train, fromCode, toCode, journeyDate);
            System.out.println("Train ID: " + train.getId() + " | " + train);

            for (TrainStop stop : bookingService.stops(train.getId())) {
                String day = stop.getDayOffset() > 0
                        ? " (Day " + (stop.getDayOffset() + 1) + ")"
                        : "";

                System.out.println("  " + stop.getStopOrder() + ". "
                        + stop.getStation().getName()
                        + " | Arrival: " + formatTime(stop.getArrivalTime())
                        + " | Departure: " + formatTime(stop.getDepartureTime()) + day);
            }
        }

        System.out.print("Enter the train ID you want to book: ");
        Long trainId;
        try {
            trainId = Long.parseLong(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Please enter a valid train ID.");
            return null;
        }

        boolean listed = false;
        for (Train train : trains) {
            if (train.getId().equals(trainId)) {
                listed = true;
            }
        }
        if (!listed) {
            System.out.println("That train is not in the search results.");
            return null;
        }

        List<TrainClass> classes = bookingService.classes(trainId);
        if (classes.isEmpty()) {
            System.out.println("No booking classes are available for this train.");
            return null;
        }

        System.out.println("\nAvailable classes:");
        for (TrainClass trainClass : classes) {
            int availableSeats = bookingService.seatsLeft(
                    trainId, trainClass.getCoachClass().getCode(), journeyDate);

            System.out.println(trainClass.getCoachClass().getCode() + " - "
                    + trainClass.getCoachClass().getName()
                    + " | Available seats: " + availableSeats);
        }

        System.out.print("Enter the class code: ");
        String classCode = scanner.nextLine().trim().toUpperCase();

        try {
            if (travellers.isEmpty()) {
                System.out.print("Enter the number of passengers (1 to 6): ");
                int passengerCount = Integer.parseInt(scanner.nextLine().trim());

                if (passengerCount < 1 || passengerCount > 6) {
                    System.out.println("A booking must include between 1 and 6 passengers.");
                    return null;
                }

                for (int i = 1; i <= passengerCount; i++) {
                    System.out.println("\nPassenger " + i + " details");

                    System.out.print("Full name: ");
                    String name = scanner.nextLine().trim();

                    System.out.print("Age: ");
                    int age = Integer.parseInt(scanner.nextLine().trim());

                    System.out.print("Gender (M/F/O): ");
                    String gender = scanner.nextLine().trim().toUpperCase();

                    new BookingPassenger(name, age, gender); // only checks the details are valid
                    travellers.add(new PassengerInfo(name, age, gender));
                }
            }

            List<BookingPassenger> passengers = new ArrayList<>();
            for (PassengerInfo info : travellers) {
                passengers.add(new BookingPassenger(info.name(), info.age(), info.gender()));
            }

            return bookingService.book(
                    passenger, trainId, classCode, fromCode, toCode, journeyDate, passengers);

        } catch (NumberFormatException e) {
            System.out.println("Please enter a number.");
            travellers.clear();
            return null;
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
            travellers.clear();
            return null;
        }
    }

    private static String formatTime(LocalTime time) {
        return time == null ? "Not available" : time.toString();
    }

    // Prints one ticket with the departure and arrival date and time for the booked stations.
    private static void printTicket(BookingService bookingService, Booking booking) {
        String fromCode = booking.getFromStation().getCode();
        String toCode = booking.getToStation().getCode();

        TrainStop fromStop = null;
        TrainStop toStop = null;
        for (TrainStop stop : bookingService.stops(booking.getTrain().getId())) {
            if (stop.getStation().getCode().equals(fromCode)) {
                fromStop = stop;
            }
            if (stop.getStation().getCode().equals(toCode)) {
                toStop = stop;
            }
        }

        System.out.println("----------------------------------------");
        System.out.println("PNR: " + booking.getPnr() + " | Status: " + booking.getStatus());
        System.out.println("Train: " + booking.getTrain());
        System.out.println("Class: " + booking.getCoachClass().getCode()
                + " | Seats: " + booking.getSeatCount());

        if (fromStop != null && toStop != null) {
            LocalDate departDate = booking.getJourneyDate().plusDays(fromStop.getDayOffset());
            LocalDate arriveDate = booking.getJourneyDate().plusDays(toStop.getDayOffset());

            System.out.println("From: " + booking.getFromStation().getName()
                    + " | Departure: " + departDate + " " + formatTime(fromStop.getDepartureTime()));
            System.out.println("To:   " + booking.getToStation().getName()
                    + " | Arrival:   " + arriveDate + " " + formatTime(toStop.getArrivalTime()));
        }
    }

    private static void printTrainSummary(BookingService bookingService, Train train,
                                          String fromCode, String toCode, LocalDate journeyDate) {
        TrainStop fromStop = null;
        TrainStop toStop = null;

        for (TrainStop stop : bookingService.stops(train.getId())) {
            if (stop.getStation().getCode().equals(fromCode)) {
                fromStop = stop;
            }
            if (stop.getStation().getCode().equals(toCode)) {
                toStop = stop;
            }
        }

        if (fromStop == null || toStop == null) {
            return;
        }

        LocalDate departDate = journeyDate.plusDays(fromStop.getDayOffset());
        LocalDate arriveDate = journeyDate.plusDays(toStop.getDayOffset());

        System.out.println(train + " | "
                + fromStop.getStation().getName() + " (" + departDate + " "
                + formatTime(fromStop.getDepartureTime()) + ")  ->  "
                + toStop.getStation().getName() + " (" + arriveDate + " "
                + formatTime(toStop.getArrivalTime()) + ")");
    }

    private static void showOtherDates(BookingService bookingService, String fromCode, String toCode) {
        List<Train> routeTrains = bookingService.searchRoute(fromCode, toCode);

        if (routeTrains.isEmpty()) {
            System.out.println("No train runs directly from " + fromCode + " to " + toCode + ".");
            return;
        }

        System.out.println("No trains on that date, but this route is available on:");

        for (Train train : routeTrains) {
            List<DayOfWeek> days = bookingService.runningDays(train.getId());
            System.out.println(train + " runs on: " + days);

            int shown = 0;
            LocalDate date = bookingService.startDate();
            while (!date.isAfter(bookingService.endDate()) && shown < 5) {
                if (days.contains(date.getDayOfWeek())) {
                    System.out.println("   " + date + " (" + date.getDayOfWeek() + ")");
                    shown++;
                }
                date = date.plusDays(1);
            }
        }
    }

    private static void adminLogin(BookingService bookingService) {
        System.out.println("\n===== ADMIN LOGIN =====");

        System.out.print("Admin username: ");
        String username = scanner.nextLine().trim();

        System.out.print("Password: ");
        String password = scanner.nextLine();

        User admin = bookingService.login(username, password, UserRole.ADMIN);

        if (admin == null) {
            System.out.println("Admin login failed. Please check your credentials.");
            return;
        }

        showAdminMenu(bookingService);
    }

    private static void showAdminMenu(BookingService bookingService) {
        boolean loggedIn = true;

        while (loggedIn) {
            System.out.println("\n===== ADMIN MENU =====");
            System.out.println("1. View all bookings");
            System.out.println("2. View all stations");
            System.out.println("3. Logout");
            System.out.print("Enter your choice: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> showAllBookings(bookingService);
                case "2" -> bookingService.stations().forEach(System.out::println);
                case "3" -> loggedIn = false;
                default -> System.out.println("Please enter a valid option.");
            }
        }
    }

    private static void showAllBookings(BookingService bookingService) {
        List<Booking> bookings = bookingService.all();

        if (bookings.isEmpty()) {
            System.out.println("There are no bookings in the system yet.");
            return;
        }

        System.out.println("\n===== ALL BOOKINGS =====");
        for (Booking booking : bookings) {
            System.out.println(
                    "PNR: " + booking.getPnr()
                            + " | Passenger: " + booking.getUser().getUsername()
                            + " | Train: " + booking.getTrain().getTrainName()
                            + " | Route: " + booking.getFromStation().getCode()
                            + " -> " + booking.getToStation().getCode()
                            + " | Journey date: " + booking.getJourneyDate()
                            + " | Status: " + booking.getStatus()
            );
        }
    }
}
