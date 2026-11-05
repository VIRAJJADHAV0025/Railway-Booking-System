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
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        name = "train_classes",
        uniqueConstraints = @UniqueConstraint(columnNames = {"train_id", "class_code"})
)
public class TrainClass {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "train_id")
    private Train train;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "class_code")
    private CoachClass coachClass;

    @Column(name = "total_seats", nullable = false)
    private int totalSeats;

    protected TrainClass() {
        // Required by Hibernate.
    }

    public TrainClass(Train train, CoachClass coachClass, int totalSeats) {
        if (totalSeats < 1) {
            throw new IllegalArgumentException("Seat capacity must be greater than zero.");
        }

        this.train = train;
        this.coachClass = coachClass;
        this.totalSeats = totalSeats;
    }

    public Long getId() {
        return id;
    }

    public CoachClass getCoachClass() {
        return coachClass;
    }

    public int getTotalSeats() {
        return totalSeats;
    }
}
