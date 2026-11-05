package com.railway;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);

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

        boolean registered = bookingService.register(username, password, name);

        if (registered) {
            System.out.println("Your account has been created successfully.");
        } else {
            System.out.println("That username is already in use. Please choose another one.");
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
            System.out.println(
                    booking.getPnr() + " | "
                            + booking.getTrain().getTrainName() + " | "
                            + booking.getJourneyDate() + " | "
                            + booking.getCoachClass().getCode() + " | "
                            + booking.getStatus()
            );
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

        List<Station> stations = bookingService.stations();
        System.out.println("\nAvailable stations:");
        stations.forEach(System.out::println);

        System.out.print("Enter the source station code: ");
        String fromCode = scanner.nextLine().trim().toUpperCase();

        System.out.print("Enter the destination station code: ");
        String toCode = scanner.nextLine().trim().toUpperCase();

        System.out.print("Enter the journey date (YYYY-MM-DD): ");
        LocalDate journeyDate = LocalDate.parse(scanner.nextLine().trim());

        List<Train> trains = bookingService.search(fromCode, toCode, journeyDate);

        if (trains.isEmpty()) {
            System.out.println("No trains were found for the selected route and date.");
            return;
        }

        System.out.println("\n===== AVAILABLE TRAINS =====");
        for (Train train : trains) {
            System.out.println("Train ID: " + train.getId() + " | " + train);

            List<TrainStop> stops = bookingService.stops(train.getId());
            for (TrainStop stop : stops) {
                String arrival = formatTime(stop.getArrivalTime());
                String departure = formatTime(stop.getDepartureTime());
                String day = stop.getDayOffset() > 0
                        ? " (Day " + (stop.getDayOffset() + 1) + ")"
                        : "";

                System.out.println("  " + stop.getStopOrder() + ". "
                        + stop.getStation().getName()
                        + " | Arrival: " + arrival
                        + " | Departure: " + departure + day);
            }
        }

        System.out.print("Enter the train ID you want to book: ");
        Long trainId = Long.parseLong(scanner.nextLine().trim());

        List<TrainClass> classes = bookingService.classes(trainId);
        if (classes.isEmpty()) {
            System.out.println("No booking classes are available for this train.");
            return;
        }

        System.out.println("\nAvailable classes:");
        for (TrainClass trainClass : classes) {
            int availableSeats = bookingService.seatsLeft(
                    trainId,
                    trainClass.getCoachClass().getCode(),
                    journeyDate
            );

            System.out.println(trainClass.getCoachClass().getCode() + " - "
                    + trainClass.getCoachClass().getName()
                    + " | Available seats: " + availableSeats);
        }

        System.out.print("Enter the class code: ");
        String classCode = scanner.nextLine().trim().toUpperCase();

        System.out.print("Enter the number of passengers (1 to 6): ");
        int passengerCount = Integer.parseInt(scanner.nextLine().trim());

        if (passengerCount < 1 || passengerCount > 6) {
            System.out.println("A booking must include between 1 and 6 passengers.");
            return;
        }

        List<BookingPassenger> passengers = new ArrayList<>();
        for (int i = 1; i <= passengerCount; i++) {
            System.out.println("\nPassenger " + i + " details");

            System.out.print("Full name: ");
            String name = scanner.nextLine().trim();

            System.out.print("Age: ");
            int age = Integer.parseInt(scanner.nextLine().trim());

            System.out.print("Gender (M/F/O): ");
            String gender = scanner.nextLine().trim().toUpperCase();

            passengers.add(new BookingPassenger(name, age, gender));
        }

        String pnr = bookingService.book(
                passenger,
                trainId,
                classCode,
                journeyDate,
                passengers
        );

        System.out.println("\nYour booking has been confirmed.");
        System.out.println("Your PNR number is: " + pnr);
    }

    private static String formatTime(LocalTime time) {
        return time == null ? "Not available" : time.toString();
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
                            + " | Journey date: " + booking.getJourneyDate()
                            + " | Status: " + booking.getStatus()
            );
        }
    }
}
