package com.railway;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "stations")
public class Station {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 10, unique = true)
    private String code;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(length = 100)
    private String city;

    protected Station() {
        // Required by Hibernate.
    }

    public Station(String code, String name, String city) {
        this.code = code;
        this.name = name;
        this.city = city;
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return code + " - " + name + " (" + city + ")";
    }
}
