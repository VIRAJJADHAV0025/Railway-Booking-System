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
import java.time.LocalTime;

@Entity
@Table(
        name = "train_stops",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"train_id", "stop_order"}),
                @UniqueConstraint(columnNames = {"train_id", "station_id"})
        }
)
public class TrainStop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "train_id")
    private Train train;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "station_id")
    private Station station;

    @Column(name = "stop_order", nullable = false)
    private int stopOrder;

    @Column(name = "arrival_time")
    private LocalTime arrivalTime;

    @Column(name = "departure_time")
    private LocalTime departureTime;

    @Column(name = "day_offset", nullable = false)
    private int dayOffset;

    protected TrainStop() {
        // Required by Hibernate.
    }

    public TrainStop(
            Train train,
            Station station,
            int stopOrder,
            LocalTime arrivalTime,
            LocalTime departureTime,
            int dayOffset
    ) {
        this.train = train;
        this.station = station;
        this.stopOrder = stopOrder;
        this.arrivalTime = arrivalTime;
        this.departureTime = departureTime;
        this.dayOffset = dayOffset;
    }

    public int getStopOrder() {
        return stopOrder;
    }

    public Station getStation() {
        return station;
    }

    public LocalTime getArrivalTime() {
        return arrivalTime;
    }

    public LocalTime getDepartureTime() {
        return departureTime;
    }

    public int getDayOffset() {
        return dayOffset;
    }
}
