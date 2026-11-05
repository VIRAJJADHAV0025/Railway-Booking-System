package com.railway;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.DayOfWeek;
import java.util.Objects;

@Entity
@Table(name = "train_running_days")
@IdClass(TrainRunningDay.Key.class)
public class TrainRunningDay {

    @Id
    @ManyToOne
    @JoinColumn(name = "train_id")
    private Train train;

    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", length = 10)
    private DayOfWeek dayOfWeek;

    protected TrainRunningDay() {
        // Required by Hibernate.
    }

    public TrainRunningDay(Train train, DayOfWeek dayOfWeek) {
        this.train = train;
        this.dayOfWeek = dayOfWeek;
    }

    public static class Key implements Serializable {

        public Long train;
        public DayOfWeek dayOfWeek;

        public Key() {
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (!(object instanceof Key other)) {
                return false;
            }
            return Objects.equals(train, other.train)
                    && dayOfWeek == other.dayOfWeek;
        }

        @Override
        public int hashCode() {
            return Objects.hash(train, dayOfWeek);
        }
    }
}
