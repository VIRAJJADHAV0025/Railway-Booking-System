package com.railway;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "bookings",
        indexes = @Index(
                name = "idx_seats",
                columnList = "train_id,class_code,journey_date,status"
        )
)
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 10)
    private String pnr;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "train_id")
    private Train train;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "class_code")
    private CoachClass coachClass;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "from_station_id")
    private Station fromStation;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "to_station_id")
    private Station toStation;

    @Column(name = "journey_date", nullable = false)
    private LocalDate journeyDate;

    @Column(name = "seat_count", nullable = false)
    private int seatCount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BookingStatus status = BookingStatus.CONFIRMED;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(
            mappedBy = "booking",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.EAGER
    )
    private List<BookingPassenger> passengers = new ArrayList<>();

    protected Booking() {
        // Required by Hibernate.
    }

    public Booking(
            String pnr,
            User user,
            Train train,
            CoachClass coachClass,
            Station fromStation,
            Station toStation,
            LocalDate journeyDate,
            int seatCount
    ) {
        this.pnr = pnr;
        this.user = user;
        this.train = train;
        this.coachClass = coachClass;
        this.fromStation = fromStation;
        this.toStation = toStation;
        this.journeyDate = journeyDate;
        this.seatCount = seatCount;
        this.createdAt = LocalDateTime.now();
    }

    public void addPassenger(BookingPassenger passenger) {
        passengers.add(passenger);
        passenger.setBooking(this);
    }

    public Long getId() {
        return id;
    }

    public String getPnr() {
        return pnr;
    }

    public User getUser() {
        return user;
    }

    public Train getTrain() {
        return train;
    }

    public CoachClass getCoachClass() {
        return coachClass;
    }

    public Station getFromStation() {
        return fromStation;
    }

    public Station getToStation() {
        return toStation;
    }

    public LocalDate getJourneyDate() {
        return journeyDate;
    }

    public int getSeatCount() {
        return seatCount;
    }

    public BookingStatus getStatus() {
        return status;
    }
}
