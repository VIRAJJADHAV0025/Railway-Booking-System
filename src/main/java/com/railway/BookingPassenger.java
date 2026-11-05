package com.railway;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "booking_passengers")
public class BookingPassenger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_id")
    private Booking booking;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private int age;

    @Column(nullable = false, length = 1, columnDefinition = "CHAR(1)")
    private String gender;

    @Column(name = "seat_label", length = 10)
    private String seatLabel;

    protected BookingPassenger() {
        // Required by Hibernate.
    }

    public BookingPassenger(String name, int age, String gender) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Please enter the passenger's name.");
        }
        if (age < 0 || age > 120) {
            throw new IllegalArgumentException("Please enter a valid age.");
        }
        if (gender == null || !gender.matches("[MFO]")) {
            throw new IllegalArgumentException("Gender must be M, F or O.");
        }

        this.name = name.trim();
        this.age = age;
        this.gender = gender;
    }

    void setBooking(Booking booking) {
        this.booking = booking;
    }
}
